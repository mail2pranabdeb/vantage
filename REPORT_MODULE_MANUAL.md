# Report Module - User Manual

## Overview

The Report Module allows you to design, execute, and schedule reports from your database. You can create reports visually using drag-and-drop or write SQL directly, then schedule them to run automatically and email the results.

---

## Quick Start

### 1. Access Reports
Go to **Report Management** in the sidebar (under Report Management menu).

### 2. Create Your First Report
1. Click **New Report** → Opens Report Designer
2. Select a **Datasource** (e.g., your main database)
3. Enter a **Template Name** (e.g., "User List Report")
4. Enter a **Template Key** (e.g., "user_list")
5. Go to **Columns tab** → Add columns from tables
6. Click **Preview** to see results
7. Click **Save**

---

## Report Designer

### Tabs Overview

#### Datasource Tab
- Select which database to query
- Enter report name and key (unique identifier)
- Add description for documentation

#### Columns Tab
- **Left Panel**: Available tables and columns (system tables excluded)
  - Search box to filter tables
  - Pagination (5 tables per page)
  - Click **Add** to include a column
- **Right Panel**: Selected columns
  - Set **Alias** for display names
  - Set **Width** for export formatting
  - Click **X** to remove

#### SQL Tab
- View auto-generated SQL from your column selection
- Edit SQL manually if needed
- Use `:paramName` for parameters (e.g., `WHERE status = :status`)

#### Preview Tab
- Click **Preview** button at top
- Shows generated SQL
- Shows result count and data table

#### Email & Schedule Tab
- Select output format (Excel, CSV, HTML, JSON)
- See instructions for scheduling

---

## Managing Reports

### Report Management Page

| Action | Button | Description |
|--------|--------|-------------|
| **Edit in Designer** | `<Code>` icon | Opens report in Report Designer |
| **Execute** | `<Play>` icon | Runs report immediately |
| **Schedule** | `<Calendar>` icon | Sets up automatic execution with email |
| **Export** | `<Download>` icon | Downloads current results |
| **Delete** | `<Trash>` icon | Removes the report |

### Create New Report
1. Click **New Report**
2. Design in Report Designer (see above)
3. Save the template
4. Return to Report Management to schedule

---

## Scheduling Reports

### Schedule a Report
1. Click the **Calendar icon** next to any report
2. Choose a **Cron preset** or enter custom expression:
   - `0 0 9 * * ?` → Daily at 9 AM
   - `0 0 * * * ?` → Every hour
   - `0 0 9 ? * MON-FRI` → Weekdays at 9 AM
3. Enter **Email Recipients** (comma-separated)
4. (Optional) Add CC emails, custom subject, and body
5. Select attachment format
6. Click **Schedule**

### What Happens When Scheduled?
- A Quartz job is created automatically
- At scheduled time, the report runs
- Results are attached to an email
- Email is sent to all recipients

### Unschedule a Report
- Jobs created through the Job Scheduling module can be managed there
- To remove a scheduled report, delete the associated job

---

## Export Formats

| Format | Extension | Best For |
|--------|-----------|----------|
| **Excel** | `.xls` | Data analysis, pivot tables |
| **CSV** | `.csv` | Import to other systems |
| **HTML** | `.html` | View in browser |
| **JSON** | `.json` | API integrations |

---

## Common Use Cases

### Daily Sales Report
1. Create report with sales data columns
2. Schedule: `0 0 9 * * ?` (daily 9 AM)
3. Email: `sales@company.com, manager@company.com`
4. Format: Excel

### Weekly Summary
1. Create report with summary SQL (GROUP BY, aggregates)
2. Schedule: `0 0 12 ? * SUN` (Sunday noon)
3. Email: `executives@company.com`
4. Format: HTML (viewable in email body)

### Monthly Audit Report
1. Create report with audit trail columns
2. Schedule: `0 0 9 1 * ?` (1st of month)
3. Email: `audit@company.com`
4. Format: Excel with filters

---

## Troubleshooting

### Preview Shows No Data
- Check that columns are added in Columns tab
- Verify datasource is selected
- Check SQL tab for valid query

### Schedule Not Working
- Verify cron expression is valid
- Ensure email recipients are correct
- Check SMTP configuration in System Settings

### Export File Empty
- Execute the report first to verify data
- Check that the datasource connection is active

---

## Tips

- **Save often** while designing reports
- **Test with Preview** before scheduling
- **Use descriptive names** for easy identification
- **Add descriptions** to document report purpose
- **Start with simple queries** then add complexity

---

## Keyboard Shortcuts

| Action | Shortcut |
|--------|----------|
| Save Report | Ctrl+S (when in designer) |
| Preview | Click Preview button |
| Export | Click Excel/CSV button |

---

## Support

For questions or issues, contact your system administrator or check the application logs.
