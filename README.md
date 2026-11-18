# Movie Rating System

Java 17 console project using JDBC and MySQL.

## Features
- User registration
- Add and view movies
- Give a 1–5 rating and write a review
- Update a user's previous rating for a movie
- Calculate average ratings and rank movies
- Display reviews
- Recommend unrated movies in the genre the user rates most highly

## Requirements
- JDK 17+
- MySQL Server
- Maven
- VS Code with Extension Pack for Java

## Setup
1. Start MySQL Server.
2. Open MySQL Workbench (or MySQL command line).
3. Execute `database/schema.sql`.
4. Open this folder in VS Code.
5. In the VS Code PowerShell terminal, set your MySQL password:
   ```powershell
   $env:DB_USER="root"
   $env:DB_PASSWORD="your_mysql_password"
   ```
   Optionally set `DB_URL` if your database uses a different host or port.
6. Run:
   ```powershell
   mvn clean compile
   mvn exec:java
   ```

## GitHub
Create an empty GitHub repository named `Movie-Rating-System`, then:
```bash
git init
git add .
git commit -m "Initial Movie Rating System"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/Movie-Rating-System.git
git push -u origin main
```
Replace `YOUR_USERNAME` with your GitHub username.

**Security:** Do not put your real MySQL password in the Java source or upload it to GitHub.
