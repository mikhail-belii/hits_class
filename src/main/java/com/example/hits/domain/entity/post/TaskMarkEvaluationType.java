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

        @Override
        public boolean isAnswerScoreMustBeSetManually() { return true; };

        @Override
        public boolean isAnswerScoreIsPassFail() { return false; };

        @Override
        public boolean isAnswerScoreIsCriteriaBased() { return false; };
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

        @Override
        public boolean isAnswerScoreMustBeSetManually() { return true; };

        @Override
        public boolean isAnswerScoreIsPassFail() { return true; };

        @Override
        public boolean isAnswerScoreIsCriteriaBased() { return false; };
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

        @Override
        public boolean isAnswerScoreMustBeSetManually() { return false; };

        @Override
        public boolean isAnswerScoreIsPassFail() { return false; };

        @Override
        public boolean isAnswerScoreIsCriteriaBased() { return true; };
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

        @Override
        public boolean isAnswerScoreMustBeSetManually() { return false; };

        @Override
        public boolean isAnswerScoreIsPassFail() { return false; };

        @Override
        public boolean isAnswerScoreIsCriteriaBased() { return true; };
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

        @Override
        public boolean isAnswerScoreMustBeSetManually() { return false; };

        @Override
        public boolean isAnswerScoreIsPassFail() { return false; };

        @Override
        public boolean isAnswerScoreIsCriteriaBased() { return true; };
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

        @Override
        public boolean isAnswerScoreMustBeSetManually() { return false; };

        @Override
        public boolean isAnswerScoreIsPassFail() { return false; };

        @Override
        public boolean isAnswerScoreIsCriteriaBased() { return true; };
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

        @Override
        public boolean isAnswerScoreMustBeSetManually() { return false; };

        @Override
        public boolean isAnswerScoreIsPassFail() { return false; };

        @Override
        public boolean isAnswerScoreIsCriteriaBased() { return true; };
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

        @Override
        public boolean isAnswerScoreMustBeSetManually() { return false; };

        @Override
        public boolean isAnswerScoreIsPassFail() { return true; };

        @Override
        public boolean isAnswerScoreIsCriteriaBased() { return true; };
    };

    public abstract void validatePostCreationByMarkEvaluationType(
            TaskCreationFields taskCreationFields,
            CourseMarkEvaluationType courseMarkEvaluationType);

    public abstract boolean isAnswerScoreMustBeSetManually();

    public abstract boolean isAnswerScoreIsPassFail();

    public abstract boolean isAnswerScoreIsCriteriaBased();

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
