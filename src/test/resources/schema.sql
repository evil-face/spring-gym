CREATE TABLE TrainingType (
    id SERIAL PRIMARY KEY,
    trainingTypeName VARCHAR(255) NOT NULL
);

CREATE TABLE User_ (
    id SERIAL PRIMARY KEY,
    firstName VARCHAR(255) NOT NULL,
    lastName VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    isActive BOOLEAN NOT NULL
);

CREATE TABLE Trainee (
    id BIGINT PRIMARY KEY REFERENCES User_ (id),
    dateOfBirth DATE,
    address VARCHAR(255),
    FOREIGN KEY (id) REFERENCES User_ (id)
);

CREATE TABLE Trainer (
    id BIGINT PRIMARY KEY REFERENCES User_ (id),
    specialization BIGINT,
    FOREIGN KEY (id) REFERENCES User_ (id),
    FOREIGN KEY (specialization) REFERENCES TrainingType (id)
);

CREATE TABLE Trainee_Trainer (
    trainee_id BIGINT REFERENCES Trainee (id),
    trainer_id BIGINT REFERENCES Trainer (id),
    PRIMARY KEY (trainee_id, trainer_id)
);

CREATE TABLE Training (
    id SERIAL PRIMARY KEY,
    trainee_id BIGINT,
    trainer_id BIGINT,
    trainingName VARCHAR(255) NOT NULL,
    training_type BIGINT,
    trainingDate DATE NOT NULL,
    trainingDuration INT,
    FOREIGN KEY (trainee_id) REFERENCES User_ (id),
    FOREIGN KEY (trainer_id) REFERENCES User_ (id),
    FOREIGN KEY (training_type) REFERENCES TrainingType (id)
);



