package client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

final class Actions {
    private Actions() {}

    static Action createSendMessageAction(Runnable sendRunnable) {
        return new BaseAction("Send", "Send message",
                new ImageIcon(Actions.class.getResource("iconSend.png")), KeyEvent.VK_S, sendRunnable);
    }

    public static Action createChangeStatusAction(Runnable changeStatusRunnable) {
        return new BaseAction("Change status", "Change status visible to others",
                new ImageIcon(Actions.class.getResource("iconStatus.png")), KeyEvent.VK_C, changeStatusRunnable);
    }

    private static class BaseAction extends AbstractAction {
        private final Runnable action;

        public BaseAction(String text, String desc, ImageIcon icon, Integer mnemonic, Runnable action) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
            this.action = action;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            this.action.run();
        }
    }
}
