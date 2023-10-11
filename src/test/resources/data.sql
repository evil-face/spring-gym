INSERT INTO TrainingType (trainingTypeName) VALUES
    ('Strength Training'),
    ('Cardio Workout'),
    ('Yoga'),
    ('Pilates'),
    ('CrossFit');

INSERT INTO User_ (firstName, lastName, username, password, isActive)
VALUES
    ('Alice', 'Johnson', 'alice', 'password1', true),
    ('Bob', 'Smith', 'bob', 'password2', true),
    ('Charlie', 'Brown', 'charlie', 'password3', true),
    ('David', 'Lee', 'david', 'password4', true),
    ('Eve', 'Davis', 'eve', 'password5', true),
	('Trainer1', 'Johnson', 'trainer1', 'password6', true),
	('Trainer2', 'Smith', 'trainer2', 'password7', true),
	('Trainer3', 'Brown', 'trainer3', 'password8', true),
	('Trainer4', 'Lee', 'trainer4', 'password9', true),
	('Trainer5', 'Davis', 'trainer5', 'password10', true);

INSERT INTO Trainee (id, dateOfBirth, address)
VALUES
    (1, '1990-01-15', '123 Main St'),
    (2, '1985-05-20', '456 Elm St'),
    (3, '1988-09-10', '789 Oak St'),
    (4, '1992-03-25', '101 Pine St'),
    (5, '1995-12-02', '202 Cedar St');

INSERT INTO Trainer (id, specialization)
VALUES
    (6, 1),
    (7, 2),
    (8, 3),
    (9, 4),
    (10, 5);

INSERT INTO Training (trainee_id, trainer_id, trainingName, training_type, trainingDate, trainingDuration)
VALUES
    (1, 6, 'Strength Training 1', 1, '2023-09-20', 60),
    (2, 7, 'Cardio Workout 1', 2, '2023-09-21', 90),
    (3, 8, 'Yoga Session 1', 3, '2023-09-22', 75),
    (4, 9, 'Pilates Class 1', 4, '2023-09-23', 45),
    (5, 10, 'CrossFit Training 1', 5, '2023-09-24', 120),
    (1, 6, 'Strength Training 2', 1, '2023-09-25', 60),
    (2, 7, 'Cardio Workout 2', 2, '2023-09-26', 90),
    (3, 8, 'Yoga Session 2', 3, '2023-09-27', 75),
    (4, 9, 'Pilates Class 2', 4, '2023-09-28', 45),
    (5, 10, 'CrossFit Training 2', 5, '2023-09-29', 120),
    (1, 6, 'Strength Training 3', 1, '2023-09-30', 60),
    (2, 7, 'Cardio Workout 3', 2, '2023-10-01', 90),
    (3, 8, 'Yoga Session 3', 3, '2023-10-02', 75),
    (4, 9, 'Pilates Class 3', 4, '2023-10-03', 45),
    (5, 10, 'CrossFit Training 3', 5, '2023-10-04', 120),
    (1, 6, 'Strength Training 4', 1, '2023-10-05', 60),
    (2, 7, 'Cardio Workout 4', 2, '2023-10-06', 90),
    (3, 8, 'Yoga Session 4', 3, '2023-10-07', 75),
    (4, 9, 'Pilates Class 4', 4, '2023-10-08', 45),
    (5, 10, 'CrossFit Training 4', 5, '2023-10-09', 120),
    (3, 7, 'Cardio Training 1', 2, '2023-10-11', 55),
    (3, 7, 'Cardio Training 2', 2, '2023-10-12', 65);

INSERT INTO Trainee_Trainer (trainee_id, trainer_id)
VALUES
	(1, 6),
    (2, 7),
    (3, 7),
    (3, 8),
    (4, 9),
    (5, 10);