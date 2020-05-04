package com.github.lunatrius.core.exceptions;

import net.minecraft.util.text.translation.I18n;

public class LocalizedException extends Exception {
    public LocalizedException(final String format) {
        super(I18n.translateToLocal(format));
    }

    public LocalizedException(final String format, final Object... arguments) {
        super(I18n.translateToLocalFormatted(format, arguments));
    }
}
