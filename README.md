![Header](./simreal_data/assets/simreal-header-image-4.png)
# WebService of Simulator for Agent-based Modeling (WSim4ABM or SimReal)
> This is a WebService implementation for an Agent-based Modeling Simulator, shortly called WSim4ABM or SimReal.
> This opensource project houses remote access to High Performance Computing (HPC) resources through 
> browser based visualization for ABM simulations along with other services.

## Introduction
**WSim4ABM** (SimReal) is a web-based simulation platform for Agent-Based Modeling (ABM), built on top of the **MASON** simulation 
library for its extensibility, flexibility, and compatibility with Java ecosystems. The platform enables users to 
create, configure, and run ABM simulations seamlessly, both locally and on remote HPC resources. Key features include:

- **Web Interface and User Management**: Supports account management and secure access.
- **ABM Development Workflow**: Users can download sample ABM templates, develop simulations locally, and export them 
as runnable JAR files via **Gradle**.
- **Remote Simulation Execution**: Supports uploading and running simulations on remote HPC resources with real-time 
output visualization, including interactive charts.
- **Dynamic Parameter Configuration**: Allows users to adjust model parameters directly from the web interface without 
code modifications, using custom annotations and a dedicated annotation processor.
- **Robust Communication**: Utilizes Message Broker middleware to ensure stability and scalability in interactions 
between the simulation engine and the User Interface.
- **Dockerized Deployment**: The entire system is containerized for streamlined deployment on HPC environments.

WSim4ABM provides an end-to-end solution for developing, running, and visualizing ABM simulations, enhancing 
accessibility and scalability for researchers and developers.

![Simreal Simulation Page](./simreal_data/assets/simreal_run_page.png)

## Requirements  (Prerequisites)
The requirements to deploying and utilizing the SimReal system and workflow are:

- [Docker]()
- [Java]()
- Integrated Development Environment (IDE), preferably [Intellij]() for the system was heavily tested on it
- Web Browser

## Documentation

## Deployment of WSim4ABM

## Project structure
- `MainLayout.java` in `src/main/java` contains the navigation setup (i.e., the
  side/top bar and the main menu). This setup uses
  [App Layout](https://vaadin.com/docs/components/app-layout).
- `views` package in `src/main/java` contains the server-side Java views of your application.
- `views` folder in `frontend/` contains the client-side JavaScript views of your application.
- `themes` folder in `frontend/` contains the custom CSS styles.

## Tech Stack / Built With
The technology / frameworks / tools used in this project are:
1. [Vaadin](https://vaadin.com/) - 
2. [Spring Boot](https://spring.io/projects/spring-boot) -
3. [PostGREs](https://hub.docker.com/_/postgres) -
4. [Apache Kafka](https://hub.docker.com/r/confluentinc/cp-kafka) -
5. [MASON](https://cs.gmu.edu/~eclab/projects/mason/) -
6. [MiniReal Annotation Library](https://central.sonatype.com/artifact/io.github.panderior/minireal-annotation) -
7. [Gradle](https://gradle.org/) - 
8. [Maven](https://maven.apache.org/) -

## How to Contribute
<!-- Mention how anyone can contribute to make this project more productive or fix bugs in it.  

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change. 
Please make sure to update tests as appropriate. If you'd like to contribute, please fork the repository and make changes as you'd like. 
Pull requests are warmly welcome.

Steps to contribute:
1. Fork this repository (link to your repository)
2. Create your feature branch (git checkout -b feature/fooBar)
3. Commit your changes (git commit -am 'Add some fooBar')
4. Push to the branch (git push origin feature/fooBar)
5. Create a new Pull Request

Additionally you can create another document called CONTRIBUTING.md which gives instructions about how to contribute. 

Please read CONTRIBUTING.md for details on our code of conduct, and the process for submitting pull requests to us. -->



## License
<!-- A short snippet describing the license (MIT, Apache etc).

This project is licensed under the MIT License - see the LICENSE.md file for details

MIT © Yourname -->
