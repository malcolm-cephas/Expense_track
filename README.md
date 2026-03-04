# 💰 Expense Tracker (JavaFX + SQLite)

![Java](https://img.shields.io/badge/Java-17-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue)
![SQLite](https://img.shields.io/badge/SQLite-3.46-green)
![Maven](https://img.shields.io/badge/Maven-Build-red)

A simple desktop application for managing daily expenses.  
Built using **Java, JavaFX, SQLite, and JFreeChart**, this app helps you track where your money goes and visualize spending patterns with charts.

---

# ✨ Features

### 📝 Manage Expenses
- Add new expenses
- Edit existing expenses
- Delete expenses easily

### 🗂️ Categories
- Use default categories
- Create your own custom categories

### 📊 Visual Charts
- Pie charts to see spending distribution
- Bar charts to compare expenses
- Powered by **JFreeChart**

### 📅 Monthly Analysis
- Track expenses by month
- View category-wise spending
- Monitor daily spending trends

### 💾 Local Storage
All data is stored locally using **SQLite**.

Database location:
```
~/.expense-tracker/expenses.db
```

### 📤 Export Data
- Export monthly expenses as **CSV files**
- Useful for Excel or further analysis

### 🎨 Simple UI
- Built with **JavaFX**
- Clean and responsive interface

---

# 🛠️ Technology Stack

| Technology | Purpose |
|------------|---------|
| Java 17 | Programming language |
| JavaFX 21 | User interface |
| SQLite | Local database |
| JFreeChart | Chart visualization |
| Maven | Build tool |

---

# 🚀 Getting Started

## Requirements

Make sure you have installed:

- **Java JDK 17 or higher**
- **Maven**

---

## Option 1 — Run Using Windows Script

1. Open the `expense-tracker` folder.
2. Locate the file:

```
run.bat
```

3. If needed, update the paths in `run.bat` for:
- Java JDK
- JavaFX SDK

Example paths used in the script:

```
C:\Program Files\Eclipse Adoptium\...
C:\javafx-sdk-21.0.9\...
```

4. Double-click **run.bat** to start the application.

---

## Option 2 — Build and Run from Source

### Clone the repository

```bash
git clone https://github.com/malcolm-cephas/expense-tracker.git
cd expense-tracker
```

### Build the project

```bash
mvn clean install
```

### Run the application

```bash
mvn javafx:run
```

---

# 📁 Project Structure

```
expense-tracker
│
├── src
│   ├── main
│   │   ├── java        # Java source code
│   │   └── resources   # FXML files, CSS, images
│   │
│   └── test            # Unit tests
│
├── target              # Compiled files
├── expenses.db         # SQLite database (auto generated)
├── run.bat             # Script to run the application
├── pom.xml             # Maven configuration
└── README.md           # Documentation
```

---

# 🤝 Contributing

Contributions are welcome!

1. Fork the repository
2. Create a new branch

```bash
git checkout -b feature/new-feature
```

3. Commit your changes

```bash
git commit -m "Added new feature"
```

4. Push the branch

```bash
git push origin feature/new-feature
```

5. Open a **Pull Request**

---

# 📄 License

This project is licensed under the **MIT License**.
