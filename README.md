# Niagara N4 MQTT Driver Documentation
*Date: July 2023*
> Note: This documentation covers the process of installing the BITPOOL N4 MQTT Device Driver on to a Niagara N4 Workstation. The software module is designed for the Niagara N4 version framework and is not supported with older Niagara AX based systems.

## Table of Contents
- [Quick Start](#quick-start)
- [Setup](#setup)
  - [Driver Module Install](#driver-module-install)
- [Configuration](#configuration)
- [Build the Database](#build-the-database)
- [Publish Configuration Options](#publish-configuration-options)
- [Subscribe Configuration Options](#subscribe-configuration-options)

## Quick Start
Here is a simple procedure to get the device running:
1. Install driver.
2. Add Network and MQTT Subscribe Device from the Palette.
3. Configure Device by entering Broker Address, Port, Credentials.
4. Configure desired output, including format and if using hierarchy. Save.
5. Done, now check the Points folder to see new data from the Broker.

## Setup
This procedure will guide you through installing the MQTT driver on a Niagara N4 installation. This driver will not run on previous AX versions.

### Driver Module Install
1. Copy the `mqtt-rt.jar` file to the Niagara modules directory on your N4 installation. 
2. Use Niagara N4 Workbench to connect to the station’s platform service and install the `mqtt` module. 

## Configuration
Open Workbench and connect to the station. You can then start the configuration process. 

## Publish Configuration Options
The MQTT Publish device driver provides multiple configuration options such as:
- Broker Connection Type
- Broker Address
- Broker Port
- Broker Username
- Broker Password
- ClientID
- Publish Topic Path
- Publish Points Folder
- Data Output Type
- Data Output Format
- Debug to Console
- Debug Label

## Subscribe Configuration Options
The MQTT Subscribe device driver provides multiple configuration options such as:
- Broker Connection Type
- Broker Address
- Broker Port
- Broker Username
- Broker Password
- Subscribe Topic Path
- Data Input Type
- Device Status as Points
- Debug to Console
- Debug Label

For detailed information on these options, please refer to the main [documentation]() or join us on our [discord](https://discord.gg/77RzVzdqfA)
