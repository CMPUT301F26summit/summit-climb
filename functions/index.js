const { setGlobalOptions } = require("firebase-functions");
const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const logger = require("firebase-functions/logger");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

const db = getFirestore();

setGlobalOptions({ maxInstances: 10 });

exports.sendPushNotification = onDocumentCreated(
    "notifications/{notificationId}",
    async (event) => {
        const notification = event.data.data();

        const recipientId = notification.recipientId;

        if (!recipientId) {
            logger.info("Notification has no recipient ID.");
            return;
        }

        // get the recipient's user document
        const userDoc = await db.collection("users").doc(recipientId).get();

        if (!userDoc.exists) {
            logger.info("Recipient user does not exist.");
            return;
        }

        const user = userDoc.data();

        // check the user's notification preferences.
        if (user.notificationsOptedOut == true) {
            logger.info("Recipient has opted out of notifications.");
            return;
        }

        if (!user.fcmToken) {
            logger.info("Recipient has no FCM token.");
            return;
        }

        // send the push notification to the recipient's phone
        await getMessaging().send({
            token: user.fcmToken,
            notification: {
                title: notification.title || "Summit Climb",
                body: notification.text || "You have a new update."
            },
            data: {
                notificationId: event.params.notificationId,
                type: String(notification.type || ""),
                campsiteId: String(notification.campsiteId || ""),
                relatedId: String(notification.relatedId || "")
            }
        });

        logger.info("Push notification sent successfully.");
    }
);