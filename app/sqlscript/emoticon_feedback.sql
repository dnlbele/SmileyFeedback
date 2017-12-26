USE master
GO

CREATE DATABASE EmoticonFeedback
GO

USE EmoticonFeedback
GO

CREATE TABLE Question
(
  IDQuestion int not null primary key identity,
  Text nvarchar(50) NOT NULL,
  Active smallint NOT NULL
)
GO

CREATE TABLE Location
(
  IDLocation int not null primary key identity,
  Text nvarchar(50) NOT NULL,
  Active smallint NOT NULL
)
GO

CREATE TABLE Feedback
(
  IDFeedback int not null primary key identity,
  QuestionID int not null references Question(IDQuestion),
  LocationID int not null references Location(IDLocation),
  Grade smallint NOT NULL,
  DateAndTime DateTime NOT NULL
)
GO

CREATE PROCEDURE CreateQuestion
	@Text nvarchar(50)
AS
	BEGIN 
		INSERT INTO Question (Text, Active) VALUES(@Text, 1)
	END 
GO

CREATE PROCEDURE CreateLocation
	@Text nvarchar(50)
AS
	BEGIN
		INSERT INTO Location (Text, Active) VALUES(@Text, 1)
	END
GO

CREATE PROCEDURE ArchiveQuestion
	@IDQuestion int
AS
	BEGIN
		UPDATE Question SET Active = 0  WHERE IDQuestion = @IDQuestion
	END
GO

CREATE PROCEDURE ArchiveLocation
	@IDLocation int
AS
	BEGIN
		UPDATE Location SET Active = 0  WHERE IDLocation = @IDLocation
	END
GO

CREATE PROCEDURE CreateFeedback
	@QuestionID int,
	@LocationID int,
	@Grade int
AS
	BEGIN 
		INSERT INTO Feedback (QuestionID, LocationID, Grade, DateAndTime) VALUES(@QuestionID, @LocationID, @Grade, GETDATE())
	END 
GO

CREATE PROCEDURE SelectActiveQuestions
AS
	SELECT * FROM Question WHERE Active = 1
GO

CREATE PROCEDURE SelectActiveLocations
AS
	SELECT * FROM Location WHERE Active = 1
GO

