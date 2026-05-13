package com.example.hits.presentation.dto.course;

import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.presentation.dto.user.UserModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain=true)
public class UserCourseModel {

    private UserModel userModel;

    private UserCourseRole userRole;

    private Float score;
}
