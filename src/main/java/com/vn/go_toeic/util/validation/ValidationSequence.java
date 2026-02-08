package com.vn.go_toeic.util.validation;

import jakarta.validation.GroupSequence;
import jakarta.validation.groups.Default;

@GroupSequence({Default.class, ValidationGroups.NotBlankGroup.class, ValidationGroups.SizeGroup.class})
public interface ValidationSequence {
}

