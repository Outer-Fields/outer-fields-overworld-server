package io.mindspce.outerfieldsserver.datacontainers;

import io.mindspice.mindlib.data.geometry.IMutRec2;
import io.mindspice.mindlib.data.geometry.IMutVector2;


public class PlayerViewUpdate {
    private final IMutRec2[] UpdateRects = new IMutRec2[4];
    private final IMutVector2[] UpdateChunks = new IMutVector2[4];
    private int lastIndex = 0;

    public void reset(){
        lastIndex = 0;
    }

    public void addRect(int startX, int startY, int in)
}
