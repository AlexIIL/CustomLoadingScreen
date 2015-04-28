package alexiil.mods.load.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.ScaledResolution;

public class RenderingStatus {
    public static class FieldState<T> {
        /** One particular state of the changed field */
        public final T field;
        /** When it started, in seconds from when the loading screen started ticking. Set to -1 until it has actually
         * started. */
        private double start;
        /** When it ended, in seconds from when the loading screen started ticking. Set to -1 until it has actually
         * ended. */
        private double end;

        /** Initialises a new changeable field state, that has not started yet (but we know it exists, so we can refer to
         * it and render it as an upcoming task). */
        public FieldState(T field) {
            this(field, -1);
        }

        /** Initialises a new change field state, that starts now. */
        public FieldState(T field, double now) {
            this.field = field;
            start = now;
            end = -1;
        }

        /** Starts ticking this field.
         * 
         * @param now
         *            How long, in seconds, the loading screen has been ticking for */
        public void start(double now) {
            start = now;
        }

        /** Stops this field from ticking, and ends it. If this field has not been started yet then it starts it too.
         * 
         * @param now
         *            How long, in seconds, the loading screen has been ticking for */
        public void end(double now) {
            if (start == -1)
                start = now;
            end = now;
        }

        /** How long ago, in seconds, this was created
         * 
         * @param now */
        private double getLength(double now) {
            return end == -1 ? now - start : end - start;
        }

        /** How long ago, in seconds, this has been ended for. Returns -1 if it has not ended yet.
         * 
         * @param now
         *            How long, in seconds, the loading screen has been ticking for */
        private double getEndDiff(double now) {
            return end == -1 ? -1 : now - end;
        }

        /** How long ago, in seconds, this has been started for. Returns -1 if it has not started yet.
         * 
         * @param now
         *            How long, in seconds, the loading screen has been ticking for */
        private double getStartDiff(double now) {
            return start == -1 ? -1 : now - start;
        }
    }

    public static class ChangingField<T> {
        private final List<FieldState<T>> history = new ArrayList<FieldState<T>>();
        private int current = -1;

        /** Change the field immediately, ending the old one and starting a new one
         * 
         * @param now
         *            How long, in seconds, the loading screen has been ticking for. */
        public void changeField(T field, double now) {
            addFuture(field);
            moveOn(now);
        }

        /** End the current field immediately.
         * 
         * @param field
         * @param now
         *            How long, in seconds, the loading screen has been ticking for. */
        public void endCurrent(double now) {
            if (current != -1) {
                FieldState<T> currentField = history.get(current);
                currentField.end(now);
            }
        }

        /** End the current field (if it has not already been ended) and start the next one immediately.
         * 
         * @param now
         *            How long, in seconds, the loading screen has been ticking for. */
        public void moveOn(double now) {
            endCurrent(now);
            if (history.size() == current + 1)
                return;
            current++;
            history.get(current).start(now);
        }

        /** Adds a field as something to be started in the future. NOTE: this adds it to the last field to be started, so
         * you cannot call this out of order.
         * 
         * @param field
         * @param now */
        public void addFuture(T field) {
            history.add(new FieldState<T>(field));
        }

        /** @return The currently active field */
        public T getCurrent() {
            return getCurrentDiff(0);
        }

        /** @return The field that is difference away from the currently active field. (negative numbers are fields that
         *         have stopped, positive numbers are fields that haven't started yet) */
        public T getCurrentDiff(int diff) {
            if (current + diff < 0)
                return null;
            if (current + diff > history.size())
                return null;
            return history.get(current + diff).field;
        }

        /** @return <code>True</code> if there are future fields waiting to be started */
        public boolean hasMore() {
            return current < history.size();
        }
    }

    /** A simple pair class for status and percentage */
    public static class ProgressPair {
        public final String status;
        public final double percentage;

        public ProgressPair(String status, double percentage) {
            if (status == null)
                throw new IllegalArgumentException("Cannot have an empty progress!");
            if (percentage < 0 || percentage > 1)
                throw new IllegalArgumentException("Percentage must be between 0 and 1! (was " + percentage + ")");
            this.status = status;
            this.percentage = percentage;
        }
    }

    /** This stores a history of the statuses that have (and will) happen, and a history of child statuses that have
     * happened (and are happening). */
    public static class ProgressState {
        private final ChangingField<ProgressPair> progress;
        private final ChangingField<ProgressState> children;

        public ProgressState() {
            progress = new ChangingField<ProgressPair>();
            children = new ChangingField<ProgressState>();
        }

        // Progress

        /** @see alexiil.mods.load.render.RenderingStatus.ChangingField#changeField(java.lang.Object, double) */
        public void changeFieldProgress(ProgressPair field, double now) {
            progress.changeField(field, now);
        }

        /** @see alexiil.mods.load.render.RenderingStatus.ChangingField#endCurrent(double) */
        public void endCurrentProgress(double now) {
            progress.endCurrent(now);
        }

        /** @see alexiil.mods.load.render.RenderingStatus.ChangingField#moveOn(double) */
        public void moveProgressOn(double now) {
            progress.moveOn(now);
        }

        /** @see alexiil.mods.load.render.RenderingStatus.ChangingField#addFuture(java.lang.Object) */
        public void addFutureProgress(ProgressPair field) {
            progress.addFuture(field);
        }

        public boolean hasMoreProgress() {
            return progress.hasMore();
        }

        public ProgressPair getCurrentProgress() {
            return progress.getCurrent();
        }

        // Children

        public void pushChild(ProgressPair startingPair, double now) {
            popChild(now);
            ProgressState child = new ProgressState();
            child.changeFieldProgress(startingPair, now);
            children.changeField(child, now);
        }

        public void popChild(double now) {
            ProgressState prevChild = children.getCurrent();
            if (prevChild == null)
                return;
            while (prevChild.progress.hasMore())
                prevChild.progress.moveOn(now);
        }

        public ProgressState getCurrentChild() {
            return getCurrentChild(0);
        }

        public ProgressState getCurrentChild(int diff) {
            return children.getCurrentDiff(diff);
        }

        public boolean hasMoreChildren() {
            return children.hasMore();
        }
    }

    /** Stores a history of both the string statuses and the double percentages (between 0 and 1) */
    public final ProgressState progressState;
    private int screenWidth, screenHeight;
    private double seconds = 0;
    /** A map to store location specific variables */
    public final Map<String, Object> tempVariables = new HashMap<String, Object>(), definedVariables = new HashMap<String, Object>();

    public RenderingStatus(int width, int height) {
        progressState = new ProgressState();
        screenWidth = width;
        screenHeight = height;
    }

    public void tick(String status, double percentage, ScaledResolution res, double addedTime) {
        seconds += addedTime;
        screenWidth = res.getScaledWidth();
        screenHeight = res.getScaledHeight();
    }

    public double getSeconds() {
        return seconds;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }
}
