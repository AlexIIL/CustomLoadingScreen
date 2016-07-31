# CustomLoadingScreen
Drone.io: [![Build Status](https://drone.io/github.com/AlexIIL/BetterLoadingScreen/status.png)](https://drone.io/github.com/AlexIIL/BetterLoadingScreen/latest)

## Building from scratch
Clone this repository into an empty folder

    git clone https://github.com/AlexIIL/CustomLoadingScreen.git
    cd CustomLoadingScreen

If you are on linux, run:

    ./setupWorkspace build
    
Otherwise you need to run:

    git submodule init
    git submodule update
    cd ./AlexIILLib
    ./gradlew setupCIWorkspace
    ./gradlew build
    cd ..
    ./gradlew setupCIWorkspace
    ./gradlew build
    
The build jar files will be in /build/libs.
  
## Contributing
Fork this repository

Clone the forked repository into an empty folder

If you are on linux run

    ./setupWorkspace build
    
Otherwise you need to run

    git submodule init
    git submodule update
    cd ./AlexIILLib
    ./gradlew setupCIWorkspace
    ./gradlew build
    cd ..
    ./gradlew setupDecompWorkspace
    
If you use eclipse...

    ./gradlew eclipse
    
If you use Idea...

    ./gradlew idea
