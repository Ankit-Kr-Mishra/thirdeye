// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { isEmpty, remove } from "lodash";
import React, {
    createContext,
    FunctionComponent,
    useContext,
    useEffect,
    useState,
} from "react";
import { useLocation } from "react-router-dom";
import {
    NotificationProviderV1ContextProps,
    NotificationProviderV1Props,
    NotificationScopeV1,
    NotificationTypeV1,
    NotificationV1,
} from "./notification-provider-v1.interfaces";

export const NotificationProviderV1: FunctionComponent<
    NotificationProviderV1Props
> = ({ children }) => {
    const [notifications, setNotifications] = useState<NotificationV1[]>([]);
    const location = useLocation();

    useEffect(() => {
        // Navigation, clear all page scoped notifications
        removePageNotifications();
    }, [location.pathname]);

    const addNotification = (
        type: NotificationTypeV1,
        message: string,
        nonDismissible?: boolean,
        scope?: NotificationScopeV1,
        onDismiss?: () => void
    ): NotificationV1 => {
        const notification = {
            id: Date.now(),
            type: type || NotificationTypeV1.Info,
            message: message || "",
            nonDismissible: Boolean(nonDismissible),
            scope: scope || NotificationScopeV1.Page,
            onDismiss: onDismiss,
        };

        setNotifications((draftNotifications) => {
            const notifications = [notification, ...draftNotifications]; // Latest notification first

            // Sort notifications based on type, dismissible etc
            notifications.sort(notificationsComparator);

            return notifications;
        });

        return notification;
    };

    const removeNotification = (notification: NotificationV1): void => {
        if (!notification || isEmpty(notifications)) {
            return;
        }

        setNotifications((draftNotifications) => {
            const notifications = [...draftNotifications];
            remove(
                notifications,
                (eachNotification) => eachNotification.id === notification.id
            );

            // Invoke handler if this notification was dismissible
            if (!notification.nonDismissible) {
                notification.onDismiss && notification.onDismiss();
            }

            return notifications;
        });
    };

    const removePageNotifications = (): void => {
        if (isEmpty(notifications)) {
            return;
        }

        setNotifications((draftNotifications) => {
            const notifications = [...draftNotifications];
            remove(
                notifications,
                (eachNotification) =>
                    eachNotification.scope === NotificationScopeV1.Page
            );

            return notifications;
        });
    };

    const notificationsComparator = (
        notification1: NotificationV1,
        notification2: NotificationV1
    ): number => {
        if (notification1.nonDismissible !== notification2.nonDismissible) {
            // Always show the dismissible ones first
            return (
                +notification1.nonDismissible - +notification2.nonDismissible
            );
        }

        if (notification1.nonDismissible === notification2.nonDismissible) {
            // Within dismissible and non dismissible notifications, show the latest first
            return notification2.id - notification1.id;
        }

        return 0;
    };

    const notificationProviderV1Context = {
        notifications: notifications,
        notify: addNotification,
        remove: removeNotification,
    };

    return (
        <NotificationProviderV1Context.Provider
            value={notificationProviderV1Context}
        >
            {children}
        </NotificationProviderV1Context.Provider>
    );
};

const NotificationProviderV1Context =
    createContext<NotificationProviderV1ContextProps>(
        {} as NotificationProviderV1ContextProps
    );

export const useNotificationProviderV1 =
    (): NotificationProviderV1ContextProps => {
        return useContext(NotificationProviderV1Context);
    };