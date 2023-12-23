package io.mindspce.outerfieldsserver.components;

import io.mindspce.outerfieldsserver.enums.ComponentType;


public record Component<T>(
        ComponentType type,
        T componentData
) { }
