package com.example.hits.domain.entity.post;

import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.domain.entity.course.CourseMarkEvaluationType;
import com.example.hits.presentation.request.post.TaskCreationFields;

public enum TaskMarkEvaluationType {
    TEACHER_DECISION {
        @Override
        public void validatePostCreationByMarkEvaluationType(TaskCreationFields taskCreationFields, CourseMarkEvaluationType courseMarkEvaluationType) {
            validatePostCreationByCourse(taskCreationFields, courseMarkEvaluationType);
            if (taskCreationFields.getPassThreshold() != null) {
                throw ExceptionUtility.badRequestException("Pass threshold must be null according to mark evaluation type");
            }
            if (taskCreationFields.getMinScore() == null || taskCreationFields.getMaxScore() == null) {
                throw ExceptionUtility.badRequestException("Task minScore and maxScore must be not null according to mark evaluation type");
            }
        }
    },
    TEACHER_DECISION_PASS_FAIL {
        @Override
        public void validatePostCreationByMarkEvaluationType(TaskCreationFields taskCreationFields, CourseMarkEvaluationType courseMarkEvaluationType) {
            validatePostCreationByCourse(taskCreationFields, courseMarkEvaluationType);
            if (taskCreationFields.getPassThreshold() != null) {
                throw ExceptionUtility.badRequestException("Pass threshold must be null according to mark evaluation type");
            }
            if (taskCreationFields.getMinScore() != null || taskCreationFields.getMaxScore() != null) {
                throw ExceptionUtility.badRequestException("Task minScore and maxScore must be null according to mark evaluation type");
            }
        }
    },
    SUM {
        @Override
        public void validatePostCreationByMarkEvaluationType(TaskCreationFields taskCreationFields, CourseMarkEvaluationType courseMarkEvaluationType) {
            validatePostCreationByCourse(taskCreationFields, courseMarkEvaluationType);
            if (taskCreationFields.getPassThreshold() != null) {
                throw ExceptionUtility.badRequestException("Pass threshold must be null according to mark evaluation type");
            }
            if (taskCreationFields.getMinScore() == null || taskCreationFields.getMaxScore() == null) {
                throw ExceptionUtility.badRequestException("Task minScore and maxScore must be not null according to mark evaluation type");
            }
        }
    },
    MEAN_VALUE {
        @Override
        public void validatePostCreationByMarkEvaluationType(TaskCreationFields taskCreationFields, CourseMarkEvaluationType courseMarkEvaluationType) {
            validatePostCreationByCourse(taskCreationFields, courseMarkEvaluationType);
            if (taskCreationFields.getPassThreshold() != null) {
                throw ExceptionUtility.badRequestException("Pass threshold must be null according to mark evaluation type");
            }
            if (taskCreationFields.getMinScore() == null || taskCreationFields.getMaxScore() == null) {
                throw ExceptionUtility.badRequestException("Task minScore and maxScore must be not null according to mark evaluation type");
            }
        }
    },
    COEFFICIENTS_SUM {
        @Override
        public void validatePostCreationByMarkEvaluationType(TaskCreationFields taskCreationFields, CourseMarkEvaluationType courseMarkEvaluationType) {
            validatePostCreationByCourse(taskCreationFields, courseMarkEvaluationType);
            if (taskCreationFields.getPassThreshold() != null) {
                throw ExceptionUtility.badRequestException("Pass threshold must be null according to mark evaluation type");
            }
            if (taskCreationFields.getMinScore() == null || taskCreationFields.getMaxScore() == null) {
                throw ExceptionUtility.badRequestException("Task minScore and maxScore must be not null according to mark evaluation type");
            }
        }
    },
    COEFFICIENTS_MEAN_VALUE {
        @Override
        public void validatePostCreationByMarkEvaluationType(TaskCreationFields taskCreationFields, CourseMarkEvaluationType courseMarkEvaluationType) {
            validatePostCreationByCourse(taskCreationFields, courseMarkEvaluationType);
            if (taskCreationFields.getPassThreshold() != null) {
                throw ExceptionUtility.badRequestException("Pass threshold must be null according to mark evaluation type");
            }
            if (taskCreationFields.getMinScore() == null || taskCreationFields.getMaxScore() == null) {
                throw ExceptionUtility.badRequestException("Task minScore and maxScore must be not null according to mark evaluation type");
            }
        }
    },
    SELF_ASSESSMENT {
        @Override
        public void validatePostCreationByMarkEvaluationType(TaskCreationFields taskCreationFields, CourseMarkEvaluationType courseMarkEvaluationType) {
            validatePostCreationByCourse(taskCreationFields, courseMarkEvaluationType);
            if (taskCreationFields.getPassThreshold() != null) {
                throw ExceptionUtility.badRequestException("Pass threshold must be null according to mark evaluation type");
            }
            if (taskCreationFields.getMinScore() == null || taskCreationFields.getMaxScore() == null) {
                throw ExceptionUtility.badRequestException("Task minScore and maxScore must be not null according to mark evaluation type");
            }
        }
    },
    PASS_FAIL {
        @Override
        public void validatePostCreationByMarkEvaluationType(TaskCreationFields taskCreationFields, CourseMarkEvaluationType courseMarkEvaluationType) {
            validatePostCreationByCourse(taskCreationFields, courseMarkEvaluationType);
            if (taskCreationFields.getPassThreshold() == null) {
                throw ExceptionUtility.badRequestException("Pass threshold must be not null according to mark evaluation type");
            }
            if (taskCreationFields.getPassThreshold() < 0 || taskCreationFields.getPassThreshold() > 1) {
                throw ExceptionUtility.badRequestException("Pass threshold must be between 0 and 1 according to mark evaluation type");
            }
            if (taskCreationFields.getMinScore() != null || taskCreationFields.getMaxScore() != null) {
                throw ExceptionUtility.badRequestException("Task minScore and maxScore must be null according to mark evaluation type");
            }
        }
    };

    public abstract void validatePostCreationByMarkEvaluationType(
            TaskCreationFields taskCreationFields,
            CourseMarkEvaluationType courseMarkEvaluationType);

    private static void validatePostCreationByCourse(TaskCreationFields taskCreationFields, CourseMarkEvaluationType courseMarkEvaluationType) {
        if (courseMarkEvaluationType.isTaskShouldContainEvaluationFunctionByCourseMarkEvaluationType()) {
            if (taskCreationFields.getEvaluationFunction() == null) {
                throw ExceptionUtility.badRequestException("Multiplier evaluation function must be not null according to course mark evaluation type");
            }
        } else {
            if (taskCreationFields.getEvaluationFunction() != null) {
                throw ExceptionUtility.badRequestException("Multiplier evaluation function must be null according to course mark evaluation type");
            }
        }
        if (courseMarkEvaluationType.isTaskShouldContainMultiplierByCourseMarkEvaluationType()) {
            if (taskCreationFields.getMultiplier() == null) {
                throw ExceptionUtility.badRequestException("Multiplier must be not null according to course mark evaluation type");
            }
        } else {
            if (taskCreationFields.getMultiplier() != null) {
                throw ExceptionUtility.badRequestException("Multiplier must be null according to course mark evaluation type");
            }
        }
    };

}
