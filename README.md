Expense Tracker (JavaFX + SQLite)

A desktop expense-management application built using Java, JavaFX, SQLite, and JFreeChart.  
The app allows users to add/edit/delete expenses, categorize them, view monthly analytics, and generate charts.

How to run:
Navigate into expense-tracker folder
double click and execute the run.bat file, a command prompt window and the expense tracker app will appear


Features

- Add, edit, and delete expenses  
- Auto-created categories + ability to type your own  
- SQLite database (no server required)  
- Monthly statistics (per category and per day)  
- Pie chart & bar chart analytics (JFreeChart)  
- Export monthly data to CSV  
- Clean JavaFX UI  


📦 Project Structure
expense-tracker/
│
├── src/ # Java source code
├── target/
│ ├── expense-tracker-1.0-SNAPSHOT.jar
│ └── dependency/ # required libraries
│
├── expenses.db # SQLite database (auto created on first run)
├── run.bat # <--- Double-click to run the app
└── README.md (this file)
