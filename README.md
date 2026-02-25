# 💰 Expense Tracker (JavaFX + SQLite)

![Java](https://img.shields.io/badge/Java-17-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue)
![SQLite](https://img.shields.io/badge/SQLite-3.46-green)
![Maven](https://img.shields.io/badge/Maven-Build-red)

A robust desktop expense-management application built using **Java**, **JavaFX**, **SQLite**, and **JFreeChart**.
Effortlessly track your spending, categorize expenses, and visualize your financial habits with interactive charts.

---

## ✨ Features

- **📝 CRUD Operations**: Add, edit, and delete expenses with ease.
- **🗂️ Smart Categorization**: Use auto-created categories or define your own custom categories.
- **📊 Interactive Analytics**: Visualize spending with Pie charts and Bar charts (powered by JFreeChart).
- **📅 Monthly Insights**: View detailed statistics per category and per day.
- **💾 Local Database**: All data is stored locally in your user home directory (`~/.expense-tracker/expenses.db`) for reliability.
- **📤 Export Data**: Export your monthly expense data to CSV for external analysis.
- **🎨 Modern UI**: Clean and responsive JavaFX interface.

---



## 🛠️ Tech Stack

- **Language**: Java 17
- **UI Framework**: JavaFX 21.0.4
- **Database**: SQLite (via `sqlite-jdbc` 3.46.0.0)
- **Charting**: JFreeChart 1.5.4
- **Build Tool**: Maven

---

## 🚀 Getting Started

### Prerequisites

- **Java JDK 17** or higher installed.
- **Maven** installed (for building from source).

### 📥 Installation & Running

#### Option 1: Run from Executable (Windows)

1. Navigate to the `expense-tracker` folder.
2. Edit `run.bat` if your Java installation or JavaFX SDK path differs from the script defaults.
   - **Note**: The script currently hardcodes paths to JDK (`C:\Program Files\Eclipse Adoptium\...`) and JavaFX SDK (`C:\javafx-sdk-21.0.9\...`). You may need to update these variables.
3. Double-click `run.bat`.
4. The application window will open.

#### Option 2: Build from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/expense-tracker.git
   cd expense-tracker
   ```

2. Build the project using Maven:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn javafx:run
   ```

---

## � Project Structure

```bash
expense-tracker/
├── src/
│   ├── main/
│   │   ├── java/    # Java source code (com.expensetracker...)
│   │   └── resources/ # FXML files, CSS, images
│   └── test/        # Unit tests
├── target/          # Compiled classes and JARs
├── expenses.db      # SQLite database (auto-generated)
├── run.bat          # Windows batch script to run the app
├── pom.xml          # Maven configuration
└── README.md        # Project documentation
```

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
