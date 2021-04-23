# Flotion
Test yourself on flashcards made in Notion

## Tech
* Simple Flask backend utilising the unofficial Python Notion API (will refactor to use real API once available).
* Uses chota frontend CSS library for stylings.

## How to install
* Set a FLOTION_TOKEN environment variable to be your Notion token (fetched from DevTools in Notion.so web app).
* Make a flashcards database page which has a default image cover (of red,yellow,or blue), a "Correct" number parameter, a select "Module" parameter, and a date/time "Answered" parameter.
* Replace in the flotion.js file, the MODULES const with your possible tags.
* Replace CARD_PAGE with the URL of your flashcards database

## To Do
* Refactor to use Notion API once pubilc beta out
* Make it a service users can sign up for with automatic setup of Flashcards page etc
* Fix bug which causes difficulty colour to change when it hits 4?
* Add back button in to module/difficulty selection
* Add in font stylings (bold, italic, underline, etc)
* Add in other Flotion blocks (numbered list and equations)
