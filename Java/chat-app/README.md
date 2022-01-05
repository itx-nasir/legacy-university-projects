# VoidChat - Java Chat Application

A feature-rich chat application built using Java, implementing client-server architecture with RMI (Remote Method Invocation). This project was developed as part of the Advanced Programming course.

## Features

- Real-time messaging between users
- User authentication (login/signup)
- Friend requests system
- Group chat functionality
- Voice chat capability
- File transfer between users
- Message history
- User status (online/offline)
- Email notifications
- Encrypted passwords using SHA-512

## Technical Stack

- **Language:** Java 8
- **UI Framework:** JavaFX
- **Network Protocol:** Java RMI
- **Database:** SQLite
- **Build Tool:** NetBeans
- **Additional Libraries:**
  - javax.mail for email functionality
  - SQLite JDBC driver

## Architecture

The application follows a client-server architecture:

- **Server:** Handles user authentication, message routing, and database operations
- **Client:** Provides the user interface and communicates with the server using RMI

## Getting Started

### Prerequisites

- Java 8 or higher
- NetBeans IDE
- SQLite

### Installation

1. Clone the repository
2. Open NetBeans IDE
3. Open the project from the base directory
4. Run the server:
   - Right-click on the server file
   - Select "Run"
5. Run the client:
   - Right-click on the client file
   - Select "Run"

### Usage

- **Login/Signup:** Create an account or login with existing credentials
- **Chat:**
  - Type a message to broadcast to all active clients
  - Use '@username <message>' to send private messages
  - Type 'WHOISIN' to see list of active clients
  - Type 'LOGOUT' to disconnect from server

### Default Configuration

- Default port: 1500
- Default server address: "localhost"
- Default username: "Anonymous"

## Contributing

This project was developed as part of a university course and is no longer actively maintained. However, you're welcome to fork and extend it for your own use.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Authors

- Nasir Iqbal

## Acknowledgments

- Thanks to the Advanced Programming course instructors and teaching assistants
- JavaFX community for UI components
- All contributors who helped test and improve the application