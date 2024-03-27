package org.balab.minireal.views.components;

import lombok.Getter;

@Getter
public class AvatarUpdateToken
{
    private final String child_view;

    public AvatarUpdateToken(String childView)
    {
        child_view = childView;
    }
}
