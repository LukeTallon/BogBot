# BogBot: A Personalized Discord Bot

BogBot is a private Discord bot I developed to add a bit of flair and functionality to Discord servers shared among friends. 
It is a highly customized bot, created to cater to our unique and specific needs, ensuring an engaging and entertaining experience on our servers. Essentially, it will return a random message from the group chats within a discord server every day.

## 🌟 Features
- **Message Retrieval and Reading**: Efficiently reads messages from specific channels within the Discord server.
- **Database Management**: Handles the seamless writing and managing of relevant messages to a PostgreSQL database.
- **Random Quote Shipper**: Sends random quotes to a specified Discord channel, keeping the conversation lively and engaging.
- **Dockerized Application**: Containerized using Docker for ease of deployment and scalability.

## 💻 Getting Started

### Prerequisites
- Java 17 or later
- A Discord account
- PostgreSQL Database
- Docker and Docker Compose
- Maven

### Setup & Installation
1. Clone this repository to your local machine.
   ```shell
   git clone https://github.com/LukeTallon/BogBot
   cd BogBot
   ```


2. Compile the project using Maven.       
    ```shell
    mvn clean install
    ```

3. Configure the application by creating the following files in the `src/main/resources` directory:
    - `token.yaml`: Houses the Discord token.
    - `timerConfig.yaml`: Holds configurations related to message dispatch timings.
    - `dbConfig.yaml`: Stores database connection credentials and URL.

4. Build and run the Docker containers using Docker Compose.
    ```shell
    docker-compose up --build
    ```
5. Make a channel in your server(s) called `bogbot`


6. Add the bot to your Discord server using Discord's developer portal.


7. Finally, type !setup in the `bogbot` channel in your server(s) to initialize the bot. The first start up period may take some time if your server has a lot of messages.


### 🗃️ Project Structure

`Stages`: Encompasses classes managing different stages of bot’s operations such as message retrieval and database population.

`MessageRetrieval`: Manages the reading of messages from Discord channels.

`db`: Handles database connections and interactions.

`MessageDispatch`: Manages the dispatching of messages to Discord channels.

`Utils`: Contains utility classes and methods providing functionalities like configuration loading, string manipulations, and object creations.


### 🛠️ Built With
Java Discord API (JDA): A robust library facilitating interaction with Discord.

`Maven`: For dependency management.

`PostgreSQL`: Employed as the relational database system.

`Slf4j`: Logging facade for Java.

`Docker`: For containerizing the application.

### 📄 Configuration Files
`token.yaml`: Houses the Discord token.

`timerConfig.yaml`: Holds configurations related to message dispatch timings.

`dbConfig.yaml`: Stores database connection credentials and URL.

### 🌐 Deployment
This application is containerized using Docker, and it includes a Dockerfile and a docker-compose.yml file to assist with the deployment. Below is a brief overview of these files.

`Dockerfile`
Based on the openjdk:17 image.
Defines default values for the JAR file.
Creates a directory for dependencies.
Adds application's JAR, SLF4J, its dependencies, and configuration files to the container.
Sets up the correct classpath and the entry point for the application.


`Docker Compose`
Uses version 3 of the Docker Compose file format.
Defines services for the Java application and a PostgreSQL database.
Sets up a network for communication between the services.
Configures environment variables for the PostgreSQL service.

`Usage: docker-compose up --build`

This command will build the Docker image and start the services defined in docker-compose.yml.


### 📜 License
This project is designed for personal and non-commercial use to enhance the Discord server experiences amongst friends. 
It's open for educational exploration and constructive collaborations are welcomed.

### ✒️ Author
Luke Tallon