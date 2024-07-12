package org.AniSocial.util;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

@Getter
@EqualsAndHashCode
@ToString(callSuper = true)
public class Variable {
    @NotNull private final int[] ids;
    private int pages;
    private final long seconds;

    public Variable(@NotNull int[] ids) {
        if (ids == null) {
            throw new IllegalArgumentException("ids cannot be null");
        }

        this.ids = ids;
        this.pages = 1;
        this.seconds = (System.currentTimeMillis() / 1000) - 15;
    }

    public void increasePage() {
        this.pages++;
    }

    public JSONObject toJson() {
        return new JSONObject(this);
    }
}
