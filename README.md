# BogBot - Discord Bot for Random Quotes - docker implementation

BogBot is a Discord bot designed to retrieve and deliver random quotes from your server's message history. It's a fun and interactive way to reminisce about past conversations and share memorable moments.

## Features

- Retrieve and store messages from text channels for future reference.
- Generate random quotes from the stored messages.
- Send recurring random messages to your server's text channels.
- Clean and organized codebase, making it easy to understand and extend.

## Getting Started

To get started with BogBot on your server, follow these steps:

### Prerequisites

1. **Java:** Ensure you have Java 8 or later installed on your system.

2. **PostgreSQL Database:** Set up a PostgreSQL database to store message data. You'll need the database URL, username, and password.

### Installation

1. Clone this repository to your local machine.

2. Edit the `src/main/resources/dbConfig.yaml` file with your database configuration:

    ```yaml
    dbUrl: "your-database-url"
    username: "your-database-username"
    password: "your-database-password"
    ```

3. Edit the `src/main/resources/token.yaml` file with your Discord bot token:

    ```yaml
    token: "your-discord-bot-token"
    ```

4. Build the project using Maven:

    ```
    mvn clean install
    ```

5. Run the bot:

    ```
    mvn exec:java -Dexec.mainClass="org.bog.bot.BotMain"
    ```

6. Invite the bot to your Discord server and give it the necessary permissions.


7. Create a Text Channel named `bogbot` so it has a home to post in.


8. Use the !sm and !sdb commands to allow bogbot to read in and send quotes randomly! 
Be sure to wait for the loading to complete after both of these commands, before finally entering !start

### Usage

- Use the following commands to interact with BogBot:

    - `!sm`: Retrieve and store messages from all text channels on your server.
    - `!sdb`: Write all stored messages to the database.
    - `!start`: Start sending recurring random messages to the server.

- BogBot will respond with random quotes when you use the `!start` command.


## Acknowledgments

- [JDA (Java Discord API)](https://github.com/DV8FromTheWorld/JDA) - Used for Discord bot interactions.
- [PostgreSQL](https://www.postgresql.org/) - Used as the database for message storage.

Happy quoting with BogBot!
