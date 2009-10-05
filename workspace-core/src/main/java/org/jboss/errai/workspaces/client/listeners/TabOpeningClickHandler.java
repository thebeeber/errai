package org.jboss.errai.workspaces.client.listeners;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.workspaces.client.bus.MessageBusClient;
import org.jboss.errai.bus.client.MessageCallback;
import org.jboss.errai.workspaces.client.framework.Tool;
import org.jboss.errai.workspaces.client.layout.LayoutHint;
import org.jboss.errai.workspaces.client.layout.LayoutHintProvider;
import org.jboss.errai.workspaces.client.bus.CommandMessage;
import org.jboss.errai.workspaces.client.bus.ConversationMessage;
import org.jboss.errai.bus.client.protocols.LayoutCommands;
import org.jboss.errai.bus.client.protocols.LayoutParts;

public class TabOpeningClickHandler implements ClickHandler {
    private Tool tool;

    public TabOpeningClickHandler(Tool tool) {
        this.tool = tool;
    }

    public void onClick(ClickEvent event) {
        String initSubject = tool.getId() + ":init";

        if (!MessageBusClient.isSubscribed(initSubject)) {
            MessageBusClient.subscribe(initSubject, new MessageCallback() {
                public void callback(CommandMessage message) {

                    try {
                        final Widget w = tool.getWidget();
                        w.getElement().setId(message.get(String.class, LayoutParts.DOMID));

                        RootPanel.get().add(w);

                        LayoutHint.attach(w, new LayoutHintProvider() {
                            public int getHeightHint() {
                                return Window.getClientHeight() - w.getAbsoluteTop() - 20;
                            }

                            public int getWidthHint() {
                                return Window.getClientWidth() - w.getAbsoluteLeft() - 5;
                            }
                        });

                        MessageBusClient.send(ConversationMessage.create(message));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }

        /**
         * Being capturing all message registration activity. This is necessary if you want to use the automatic
         * clean-up features and close the messaging channels when the tool instance closes.
         */
        MessageBusClient.beginCapture();

        MessageBusClient.send("org.jboss.errai.WorkspaceLayout", CommandMessage.create(LayoutCommands.OpenNewTab)
                .set(LayoutParts.ComponentID, tool.getId())
                .set(LayoutParts.IconURI, tool.getIcon().getUrl())
                .set(LayoutParts.MultipleInstances, tool.multipleAllowed())
                .set(LayoutParts.Name, tool.getName())
                .set(LayoutParts.DOMID, tool.getId() + "_" + System.currentTimeMillis())
                .set(LayoutParts.InitSubject, initSubject));
    }

}
