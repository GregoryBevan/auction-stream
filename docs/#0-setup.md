# Setup the code lab

## Prerequisites for local development

- **IntelliJ IDEA** 
  - Ensure that you have [IntelliJ IDEA](https://www.jetbrains.com/idea/download/) installed on your system.
- **Docker** 
  - Install Docker appropriate for your operating system
      - **Windows and macOS**
          - Download and install [Docker Desktop](https://www.docker.com/products/docker-desktop/)
          - After installation, verify Docker is running by checking the Docker icon in your system tray (Windows) or menu bar (macOS)
      - **Linux**
          - Follow the official Docker Engine installation guide for your specific Linux distribution
          - After installation, verify Docker is running by executing `sudo systemctl status docker`
- **Git** 
  - Checkout the following project: https://github.com/GregoryBevan/auction-stream.git
  - Open the project in IntelliJ IDEA

## Prerequisites for remote development with JetBrains Gateway and Gitpod

This guide provides step-by-step instructions to set up JetBrains Gateway for remote development using Gitpod.

- **JetBrains Gateway**
  - Ensure that [JetBrains Gateway](https://www.jetbrains.com/remote-development/gateway/) is installed on your local machine.
- **Gitpod Account**
  - Verify that you have an active [Gitpod](https://www.gitpod.io/) account with at least 2h left.

### 1: Install the Gitpod Plugin in JetBrains Gateway

1. **Launch JetBrains Gateway**.
2. **Install the Gitpod Plugin**:
    - On the Welcome screen, locate the `Install More Providers` section.
    - Find `Gitpod` in the list and click `Install`.
    - Once installed, `Gitpod` will appear under the `Remote Development` section.

### 2: Connect JetBrains Gateway to Gitpod

1. **Initiate Connection**:
    - On the JetBrains Gateway Welcome screen, select `Connect to Gitpod`.

2. **Authenticate with Gitpod**:
    - A browser window will open, prompting you to log in to your Gitpod account.
    - After logging in, authorize JetBrains Gateway to access your Gitpod workspaces.

3. **Select an IDE**:
    - After authentication, choose the JetBrains IDE you wish to use for your development.

### 3: Manage Gitpod Workspaces

1. **View Workspaces**:
    - After connecting, you'll see a list of your existing Gitpod workspaces.

2. **Create a New Workspace**:
    - Click `New Workspace` to start a new development environment.
    - Enter the following repository URL : https://github.com/GregoryBevan/auction-stream.gitx.
   
3. **Connect to a Workspace**:
    - Select a workspace from the list and click `Connect` to open it in the chosen JetBrains IDE.

## Additional Resources

- **Gitpod Documentation**: For more detailed information, refer to the official [Gitpod documentation on JetBrains integration](https://www.gitpod.io/docs/integrations/jetbrains-gateway).
- **Video Tutorial**: Watch the following video for a visual guide on setting up JetBrains Gateway with Gitpod:

  [![Getting Started with JetBrains on Gitpod](https://img.youtube.com/vi/OagRlSptc2g/0.jpg)](https://www.youtube.com/watch?v=OagRlSptc2g)

By following these steps, you can seamlessly integrate JetBrains Gateway with Gitpod, enabling a robust remote development environment.

