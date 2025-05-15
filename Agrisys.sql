-- Opret databasen
CREATE DATABASE Agrisys;
GO


USE Agrisys;
GO

--  Opret tabel UserRole
CREATE TABLE UserRole (
    RoleID INT PRIMARY KEY,
    RoleName VARCHAR(50) NOT NULL
);
GO

-- Opret tabel User (uden reference til Invites endnu)
CREATE TABLE [User] (
    UserID INT PRIMARY KEY,
    Code BIGINT, -- bliver til foreign key senere
    Username VARCHAR(100) NOT NULL,
    PasswordHash VARCHAR(100) NOT NULL, 
    RoleID INT NOT NULL FOREIGN KEY REFERENCES UserRole(RoleID)
);
GO

-- Opret tabel Invites (med reference til User via UsedBy og CreatedBy)
CREATE TABLE Invites (
    Code BIGINT PRIMARY KEY,
    CreatedAt DATETIME2 NOT NULL,
	isAdmin BIT, 
    UsedBy INT NULL,
    UsedAt DATETIME2,
    CreatedBy INT NULL,
    FOREIGN KEY (UsedBy) REFERENCES [User](UserID),
    FOREIGN KEY (CreatedBy) REFERENCES [User](UserID)
);
GO

-- Tilføj nu foreign key til User.Code
ALTER TABLE [User]
ADD CONSTRAINT FK_User_Invites FOREIGN KEY (Code) REFERENCES Invites(Code);
GO

-- Opret tabel Pig
CREATE TABLE Pig (
    PigID BIGINT PRIMARY KEY, -- Transponder ID
    Number INT,
    [Location] INT,
    FCR FLOAT,          
    StartWeight FLOAT,
    EndWeight FLOAT,
    WeightGain FLOAT,
    FeedIntake FLOAT,
    TestDays INT,
    Duration FLOAT
);
GO

-- Opret tabel Feeding
CREATE TABLE Feeding (
    FeedingID INT IDENTITY(1,1) PRIMARY KEY,
    FeedingLocation INT,
    PigID BIGINT NOT NULL FOREIGN KEY REFERENCES Pig(PigID),
    [Date] DATETIME2, 
    Duration DATETIME2,
    FeedAmountGrams FLOAT
);
GO

-- View til advarsel om lavt foderindtag
CREATE VIEW PigWarnings AS
SELECT
    p.PigID,
    p.Number,
    SUM(f.FeedAmountGrams) AS TotalGramsLast3Days
FROM
    Feeding f
JOIN
    Pig p ON p.PigID = f.PigID
WHERE
    f.[Date] >= DATEADD(DAY, -3, GETDATE())
GROUP BY
    p.PigID, p.Number
HAVING
    SUM(f.FeedAmountGrams) < 1000;
GO

-- Indsæt roller
INSERT INTO UserRole (RoleID, RoleName) VALUES (1, 'USER'), (2, 'SUPERUSER');
GO

