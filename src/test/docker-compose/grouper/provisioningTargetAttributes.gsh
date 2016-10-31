grouperSession = GrouperSession.startRootSession();

addRootStem("cu", "Col U");
addStem("cu", "app", "Apps");
addStem("cu:app", "google", "Google Groups");
addGroup("cu:app:google", "test", "Test");
addStem("cu:app", "maillist", "Mailing List");
addGroup("cu:app:maillist", "test", "Test");
