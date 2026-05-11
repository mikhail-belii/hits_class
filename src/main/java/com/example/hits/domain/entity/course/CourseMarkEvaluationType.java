package com.example.hits.domain.entity.course;

import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.presentation.request.course.CourseCreationFields;

public enum CourseMarkEvaluationType {
    SUM {
        @Override
        public void validateCourseCreatingByMarkEvaluationType(CourseCreationFields courseCreationFields) {
            if (courseCreationFields.getPassThreshold() != null) {
                throw ExceptionUtility.badRequestException("Course passThreshold must be null according to mark evaluation type");
            }
        }
        @Override
        public boolean isTaskShouldContainMultiplierByCourseMarkEvaluationType() {
            return false;
        }
        @Override
        public boolean isTaskShouldContainEvaluationFunctionByCourseMarkEvaluationType() {
            return false;
        }
    },
    MEAN_VALUE {
        @Override
        public void validateCourseCreatingByMarkEvaluationType(CourseCreationFields courseCreationFields) {
            if (courseCreationFields.getPassThreshold() != null) {
                throw ExceptionUtility.badRequestException("Course passThreshold must be null according to mark evaluation type");
            }
        }
        @Override
        public boolean isTaskShouldContainMultiplierByCourseMarkEvaluationType() {
            return false;
        }
        @Override
        public boolean isTaskShouldContainEvaluationFunctionByCourseMarkEvaluationType() {
            return false;
        }
    },
    COEFFICIENTS_SUM {
        @Override
        public void validateCourseCreatingByMarkEvaluationType(CourseCreationFields courseCreationFields) {
            if (courseCreationFields.getPassThreshold() != null) {
                throw ExceptionUtility.badRequestException("Course passThreshold must be null according to mark evaluation type");
            }
        }
        @Override
        public boolean isTaskShouldContainMultiplierByCourseMarkEvaluationType() {
            return true;
        }
        @Override
        public boolean isTaskShouldContainEvaluationFunctionByCourseMarkEvaluationType() {
            return true;
        }
    },
    COEFFICIENTS_MEAN_VALUE {
        @Override
        public void validateCourseCreatingByMarkEvaluationType(CourseCreationFields courseCreationFields) {
            if (courseCreationFields.getPassThreshold() != null) {
                throw ExceptionUtility.badRequestException("Course passThreshold must be null according to mark evaluation type");
            }
        }
        @Override
        public boolean isTaskShouldContainMultiplierByCourseMarkEvaluationType() {
            return true;
        }
        @Override
        public boolean isTaskShouldContainEvaluationFunctionByCourseMarkEvaluationType() {
            return false;
        }
    },
    PASS_FAIL {
        @Override
        public void validateCourseCreatingByMarkEvaluationType(CourseCreationFields courseCreationFields) {
            if (courseCreationFields.getPassThreshold() == null) {
                throw ExceptionUtility.badRequestException("Course passThreshold must be not null according to mark evaluation type");
            }
            if (courseCreationFields.getPassThreshold() < 0 || courseCreationFields.getPassThreshold() > 1) {
                throw ExceptionUtility.badRequestException("Pass threshold must be between 0 and 1 according to mark evaluation type");
            }
        }
        @Override
        public boolean isTaskShouldContainMultiplierByCourseMarkEvaluationType() {
            return false;
        }
        @Override
        public boolean isTaskShouldContainEvaluationFunctionByCourseMarkEvaluationType() {
            return false;
        }
    };

    public abstract void validateCourseCreatingByMarkEvaluationType(CourseCreationFields courseCreationFields);

    public abstract boolean isTaskShouldContainMultiplierByCourseMarkEvaluationType();

    public abstract boolean isTaskShouldContainEvaluationFunctionByCourseMarkEvaluationType();

}
