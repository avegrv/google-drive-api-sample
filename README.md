# google-drive-api-sample

This is sample based on Google Drive API. Simple use case - using Google Drive to backup and restore SQLite Database.


### Overview

This example uses Java, SQLite DB, Google Play Services (Drive and Auth), Constraint Layout. The app connects to Google Drive to backup and restores SQLite Database. This case uses a private directory of Google Drive and the owner of Google Drive cannot get access to this folder only the app can do this. This example can help you if you want to save application info and you don't have backend or other storage. Google Drive is free storage and this solution can save your money.


### Requirements

- Connected Google Play services on a phone
- Google account to access Google Drive


### Use Case

- Click to input and write text
- Click "Write to database", the app saves your text to the SQLite
- Click "Save to Google Drive", the app uploads your DB to the Google Drive private storage
- Click "Restore from Google Drive", the app downloads your DB from the Google Drive, than restore local DB and shows string from DB in input.

<img align="center" width="210" height="400" src="https://github.com/avegrv/google-drive-api-sample/blob/master/img/img.png">
