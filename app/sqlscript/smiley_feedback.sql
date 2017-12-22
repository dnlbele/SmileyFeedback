USE master
GO

CREATE DATABASE SmileyFeedback
GO

USE SmileyFeedback
GO

CREATE TABLE Question
(
  IDQuestion int not null primary key identity,
  Text nvarchar(50) NOT NULL,
  Active smallint NOT NULL
)
GO


CREATE TABLE Feedback
(
  IDFeedback int not null primary key identity,
  QuestionID int not null references Question(IDQuestion),
  Location nvarchar(50) NOT NULL,
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

CREATE PROCEDURE ArchiveQuestion
	@IDQuestion int
AS
	BEGIN
		UPDATE Question SET Active = 0  WHERE IDQuestion = @IDQuestion
	END
GO

CREATE PROCEDURE CreateFeedback
	@QuestionID int,
	@Location nvarchar(50),
	@Grade int
AS
	BEGIN 
		INSERT INTO Feedback (QuestionID, Location, Grade, DateAndTime) VALUES(@QuestionID, @Location, @Grade, GETDATE())
	END 
GO

CREATE PROCEDURE SelectActiveQuestions
AS
	SELECT * FROM Question WHERE Active = 1
GO

