# Transportation Data Management System

## Overview

This is a Transportation Data Management System developed with Spring Boot. It provides functionality for file upload, data sorting, data searching, file merging, and exporting data. The frontend uses HTML and JavaScript with Thymeleaf template rendering, while the backend is built with Java and Spring Boot framework.

<img src="https://raw.githubusercontent.com/ToSniperSam/Transportation-Demo/refs/heads/main/fig/Overview.png" alt="256637baef4ef6c4b5084ed2c2ea761" style="zoom:67%;" />



## Quick Start

### 1. Running the Application with the JAR File

1. Download or build the JAR file.

2. Open a terminal or command prompt and navigate to the directory containing the JAR file.

3. Run the following command to start the application:

   ```bash
   java -jar transportation-system.jar
   ```

4. After the application starts, open your browser and navigate to:

   ```arduino
   http://localhost:8080
   ```

### 2. Or Running the Application with the EXE File

1. Simply double-click the EXE file to launch the application.
2. The application will start on the default port (8080).



## Features

- **File Upload**: Users can upload CSV files, which are parsed and displayed in the UI.
- **Data Sorting**: Users can select a column and sorting order (ascending or descending) to sort the data.
- **Data Search**: Users can select a column and a keyword to perform a fuzzy search on the data.
- **File Merge**: Users can upload two CSV files, which will be merged and made available for download.
- **Data Export**: Users can export the sorted or filtered data to a CSV file.



## Technologies Used

- **Frontend**: HTML, JavaScript, Thymeleaf
- **Backend**: Java, Spring Boot
- **File Handling**: CSV file processing and generation

------



## Project Structure

```bash
src/
 ├── main/
 │   ├── java/
 │   │   └── com/
 │   │       └── transportation/
 │   │           ├── controller/
 │   │           │   ├── FileUploadController.java     # File upload logic
 │   │           │   ├── SortController.java           # Sorting logic
 │   │           │   ├── SearchController.java         # Search logic
 │   │           │   ├── ExportController.java         # Export logic
 │   │           ├── model/
 │   │           │   └── FileData.java                 # Model class to store file data
 │   │           └── TransportationApplication.java    # Spring Boot Application class
 │   ├── resources/
 │   │   ├── templates/
 │   │   │   └── index.html                            # Frontend Page
 │   │   └── application.properties                    # Configuration File
 ├── target/
 │   ├── transportation-system.jar                     # JAR file
 │   └── transportation-system.exe                     # EXE file
```



## Notes

- Make sure that your server has access to the file paths and that the file size does not exceed the configured limits.
- After modifying `application.properties`, restart the application for the changes to take effect.



## Test Example

<img src="https://raw.githubusercontent.com/ToSniperSam/Transportation-Demo/refs/heads/main/fig/Search.png" alt="4a1955547761a48615dab26d01ddc1f" style="zoom:50%;" />
<img src="https://raw.githubusercontent.com/ToSniperSam/Transportation-Demo/refs/heads/main/fig/Merge.png" alt="4a1955547761a48615dab26d01ddc1f" style="zoom:50%;" />
