package com.example.hits.presentation.request.course;

import com.example.hits.domain.entity.course.CourseMarkEvaluationType;

public interface CourseCreationFields {

    String getName();

    String getDescription();

    CourseMarkEvaluationType getCourseMarkEvaluationType();

    Float getPassThreshold();

}
