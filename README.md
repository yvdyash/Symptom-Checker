# Symptom-Checker
Android App that collects COVID-19 related symptoms and stores them in a database in the smartphone

The app has 2 pages. In the first page it presents the user with two sign measurement techniques: a) heart rate sensing, and b) respiratory 
rate sensing.<br>
Following methods are used for each sensing operation.
### Heart rate sensing: 
For heart rate sensing we will utilize the back camera of the smartphone with flash 
enabled. We will take 45 s video from the back camera with the flash on. While taking the video the user 
should softly press their index finger on the camera lens while covering the flash light. From the 
variation of the red coloration in the images, the heart rate of the subject is derived. Techniques like Peak detection, denoise, calculating average frame color are used to calculate this.
### Respiratory rate: 
For respiratory rate sensing we utilized the accelerometer or orientation sensor of 
the smartphone. The user will be asked to lay down and place the smartphone on their chest for a 
period of 45 seconds. The respiratory rate will be computed from the accelerometer or orientation data. <br><br>
The user will be asked to click on the measure heart rate button and measure respiratory rate button to 
collect data from the smartphone sensors. The numbers will be stored in a database corresponding to 
the user. The user will then hit upload signs button which will create a database with the user’s lastname in the smartphone. The entry of the database will 
be a table with the first two columns heart rate and respiratory rate respectively. Each entry of the 
database will have 10 additional columns which will be filled in the next page. 
Once the user is done collecting signs data, the user will be taken to the second page to collect 
symptoms data. The user will select a symptom and then select a rating out of 5. The user does not need 
to select all the symptoms. Whichever symptoms the user has not reported will be marked with 0 rating. 
After this the user will click a upload symptoms button. 
At this point a database table entry with 12 entries will be created and stored in the database in the 
smartphone. 
