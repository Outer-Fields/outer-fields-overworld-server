package io.mindspce.outerfieldsserver.area;

import io.mindspice.mindlib.data.geometry.IVector2;


public record TileData(
        IVector2 index,
        NavData navData
) { }
