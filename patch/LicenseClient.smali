.class public Lcom/pairip/licensecheck/LicenseClient;
.super Ljava/lang/Object;
.source "LicenseClient.java"

# interfaces
.implements Landroid/content/ServiceConnection;


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;,
        Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;,
        Lcom/pairip/licensecheck/LicenseClient$DelayedTaskExecutorImpl;,
        Lcom/pairip/licensecheck/LicenseClient$DelayedTaskExecutor;
    }
.end annotation


# static fields
.field private static final BACKGROUND_SERVICE_INTERFACE_CLASS_NAME:Ljava/lang/String; = "com.android.vending.licensing.IBackgroundLicensingService"

.field private static final ERROR_INVALID_PACKAGE_NAME:I = 0x3

.field private static final EVENTUAL_SHUTDOWN_DELAY_MILLIS:I = 0x7530

.field private static final EXTRA_END_CUSTOM_TRIAL:Ljava/lang/String; = "end_custom_trial"

.field private static final FIRST_ISOLATED_UID:I = 0x182b8

.field private static final FLAG_RPC_CALL:I = 0x0

.field private static final LAST_ISOLATED_UID:I = 0x1869f

.field private static final LICENSED:I = 0x0

.field private static final MAX_RETRIES:I = 0x3

.field private static final MILLIS_PER_SEC:I = 0x3e8

.field private static final MIN_TRIAL_END_INTERVAL_MILLIS:J = 0xbb8L

.field private static final NOT_LICENSED:I = 0x2

.field private static final PAYLOAD_PAYWALL:Ljava/lang/String; = "PAYWALL_INTENT"

.field private static final PER_USER_RANGE:I = 0x186a0

.field private static final REPEATED_CHECK_RETRY_DELAY_MILLIS:I = 0x493e0

.field private static final RETRY_DELAY_MILLIS:I = 0x7d0

.field private static final SERVICE_INTERFACE_CLASS_NAME:Ljava/lang/String; = "com.android.vending.licensing.ILicensingService"

.field private static final SERVICE_PACKAGE:Ljava/lang/String; = "com.android.vending"

.field private static final TAG:Ljava/lang/String; = "LicenseClient"

.field private static final TRANSACTION_CHECK_LICENSE_V2:I = 0x2

.field private static final TRANSACTION_REPORT_SUCCESSFUL_LICENSE_CHECK:I = 0x3

.field protected static backgroundLicensingServiceEnabled:Z = true

.field protected static backgroundRunner:Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor; = null

.field protected static customTrialEndTriggered:Z = false

.field protected static eventualShutdownEnabled:Z = true

.field protected static exitAction:Ljava/lang/Runnable; = null

.field public static gracefulShutdownEnabled:Z = true

.field private static final handler:Landroid/os/Handler;

.field private static instance:Lcom/pairip/licensecheck/LicenseClient; = null

.field protected static lastTrialEndElapsedRealtimeMillis:J = 0x0L

.field protected static volatile licenseCheckState:Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState; = null

.field protected static licensePubKey:Ljava/lang/String; = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzcx3MVKn7dK6LZ7c2PA8TF1D/0PYBgjlLMQ/jHBlZ18yPItHORdRahPEP5nFfOVQMu/e+CH407hfitGslUJtMFzfsosOXV8RkDxMIVD4Iq8vY4+57BS63OGEVvn9yBUBVl6fEUf4B8VzSiDCmXRzLRzQ8BbfWWmZwTdQ3QN98b2ILxCRga7pPBWLoatvpQIJvbysWatyVat9t6NnpLrr+B+eOVFJ8oOZiSjITFmkheaPpjJf/5VeNRIzWM9fNlxvXGT9V90e+tKrmi8yyu3/qXvBPe9WSPYvFLxfFMuKlhjrD0eNcsf/aJ20ewttB9qIOyeghmhSVluKUFKYUUa1RQIDAQAB"

.field protected static localCheckEnabled:Z = false

.field protected static mainThreadRunner:Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor; = null

.field protected static packageName:Ljava/lang/String; = "org.tezza"

.field protected static repeatedCheckEnabled:Z = true

.field protected static responsePayload:Landroid/os/Bundle;


# instance fields
.field private final context:Landroid/content/Context;

.field protected delayedTaskExecutor:Lcom/pairip/licensecheck/LicenseClient$DelayedTaskExecutor;

.field protected isBound:Z

.field private repeatedCheckStartElapsedRealtime:J

.field protected retryNum:I

.field protected waitingForRepeatedCheck:Z


# direct methods
.method public static synthetic $r8$lambda$8YRQpF8qc5JOZUcKq79QHnbGjYY(Lcom/pairip/licensecheck/LicenseClient;Lcom/pairip/licensecheck/RepeatedCheckMetadata;)V
    .locals 0

    invoke-direct {p0, p1}, Lcom/pairip/licensecheck/LicenseClient;->lambda$scheduleRepeatedLicenseCheck$0(Lcom/pairip/licensecheck/RepeatedCheckMetadata;)V

    return-void
.end method

.method public static synthetic $r8$lambda$GS82Fij7VQePgSFog-s63-Rcyb0(Lcom/pairip/licensecheck/LicenseClient;)V
    .locals 0

    invoke-direct {p0}, Lcom/pairip/licensecheck/LicenseClient;->lambda$initializeLicenseCheck$0()V

    return-void
.end method

.method public static synthetic $r8$lambda$gb-vmUiJUmqdCloCudVdY-igh7I(Lcom/pairip/licensecheck/LicenseClient;Landroid/os/IBinder;)V
    .locals 0

    invoke-direct {p0, p1}, Lcom/pairip/licensecheck/LicenseClient;->lambda$onServiceConnected$1(Landroid/os/IBinder;)V

    return-void
.end method

.method public static synthetic $r8$lambda$nn58bl0tYjCXTurL0z1br2IkQ-g(Lcom/pairip/licensecheck/LicenseClient;Lcom/pairip/licensecheck/LicenseCheckException;)V
    .locals 0

    invoke-direct {p0, p1}, Lcom/pairip/licensecheck/LicenseClient;->lambda$handleError$0(Lcom/pairip/licensecheck/LicenseCheckException;)V

    return-void
.end method

.method public static synthetic $r8$lambda$ot_XkRbEJeEFG1Hy-d3H6N4DX_I(Lcom/pairip/licensecheck/LicenseClient;Lcom/pairip/licensecheck/RepeatedCheckMetadata;Landroid/os/Bundle;)V
    .locals 0

    invoke-direct {p0, p1, p2}, Lcom/pairip/licensecheck/LicenseClient;->lambda$processResponse$0(Lcom/pairip/licensecheck/RepeatedCheckMetadata;Landroid/os/Bundle;)V

    return-void
.end method

.method public static synthetic $r8$lambda$q2q7YKfx3jIZHqiUNn7fQ55wwzI(Lcom/pairip/licensecheck/LicenseClient;Z)V
    .locals 0

    invoke-direct {p0, p1}, Lcom/pairip/licensecheck/LicenseClient;->lambda$initializeLicenseCheck$1(Z)V

    return-void
.end method

.method public static synthetic $r8$lambda$tB8S6FJPE8_x6-ohvmHs84x5lek(Lcom/pairip/licensecheck/LicenseClient;ZLcom/pairip/licensecheck/LicenseCheckException;Z)V
    .locals 0

    invoke-direct {p0, p1, p2, p3}, Lcom/pairip/licensecheck/LicenseClient;->lambda$retryOrThrow$0(ZLcom/pairip/licensecheck/LicenseCheckException;Z)V

    return-void
.end method

.method public static synthetic $r8$lambda$x_INbtAE1cLJhbPOU3l3uRiKDN8(Lcom/pairip/licensecheck/LicenseClient;Z)V
    .locals 0

    invoke-direct {p0, p1}, Lcom/pairip/licensecheck/LicenseClient;->lambda$retryOrThrow$1(Z)V

    return-void
.end method

.method public static synthetic $r8$lambda$xzrAfByzooHDT9oIsgTdQvzthuE(Lcom/pairip/licensecheck/LicenseClient;Landroid/os/IBinder;)V
    .locals 0

    invoke-direct {p0, p1}, Lcom/pairip/licensecheck/LicenseClient;->lambda$onServiceConnected$0(Landroid/os/IBinder;)V

    return-void
.end method

.method static bridge synthetic -$$Nest$mprocessResponse(Lcom/pairip/licensecheck/LicenseClient;ILandroid/os/Bundle;)V
    .locals 0

    invoke-direct {p0, p1, p2}, Lcom/pairip/licensecheck/LicenseClient;->processResponse(ILandroid/os/Bundle;)V

    return-void
.end method

.method static constructor <clinit>()V
    .locals 2

    .line 78
    new-instance v0, Lcom/pairip/licensecheck/LicenseClient$1;

    invoke-direct {v0}, Lcom/pairip/licensecheck/LicenseClient$1;-><init>()V

    sput-object v0, Lcom/pairip/licensecheck/LicenseClient;->exitAction:Ljava/lang/Runnable;

    .line 116
    sget-object v0, Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;->CHECK_REQUIRED:Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;

    sput-object v0, Lcom/pairip/licensecheck/LicenseClient;->licenseCheckState:Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;

    .line 119
    new-instance v0, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda5;

    invoke-direct {v0}, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda5;-><init>()V

    sput-object v0, Lcom/pairip/licensecheck/LicenseClient;->backgroundRunner:Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;

    .line 124
    new-instance v0, Landroid/os/Handler;

    invoke-static {}, Landroid/os/Looper;->getMainLooper()Landroid/os/Looper;

    move-result-object v1

    invoke-direct {v0, v1}, Landroid/os/Handler;-><init>(Landroid/os/Looper;)V

    sput-object v0, Lcom/pairip/licensecheck/LicenseClient;->handler:Landroid/os/Handler;

    .line 126
    invoke-static {v0}, Ljava/util/Objects;->requireNonNull(Ljava/lang/Object;)Ljava/lang/Object;

    new-instance v1, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda6;

    invoke-direct {v1, v0}, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda6;-><init>(Landroid/os/Handler;)V

    sput-object v1, Lcom/pairip/licensecheck/LicenseClient;->mainThreadRunner:Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;

    const/4 v0, 0x0

    .line 131
    sput-boolean v0, Lcom/pairip/licensecheck/LicenseClient;->customTrialEndTriggered:Z

    const-wide/16 v0, 0x0

    .line 134
    sput-wide v0, Lcom/pairip/licensecheck/LicenseClient;->lastTrialEndElapsedRealtimeMillis:J

    return-void
.end method

.method public constructor <init>(Landroid/content/Context;)V
    .locals 2
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x0
        }
        names = {
            "context"
        }
    .end annotation

    .line 262
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 140
    new-instance v0, Lcom/pairip/licensecheck/LicenseClient$DelayedTaskExecutorImpl;

    const/4 v1, 0x0

    invoke-direct {v0, v1}, Lcom/pairip/licensecheck/LicenseClient$DelayedTaskExecutorImpl;-><init>(Lcom/pairip/licensecheck/LicenseClient-IA;)V

    iput-object v0, p0, Lcom/pairip/licensecheck/LicenseClient;->delayedTaskExecutor:Lcom/pairip/licensecheck/LicenseClient$DelayedTaskExecutor;

    const/4 v0, 0x0

    .line 143
    iput v0, p0, Lcom/pairip/licensecheck/LicenseClient;->retryNum:I

    .line 150
    iput-boolean v0, p0, Lcom/pairip/licensecheck/LicenseClient;->isBound:Z

    .line 157
    iput-boolean v0, p0, Lcom/pairip/licensecheck/LicenseClient;->waitingForRepeatedCheck:Z

    const-wide/16 v0, 0x0

    .line 160
    iput-wide v0, p0, Lcom/pairip/licensecheck/LicenseClient;->repeatedCheckStartElapsedRealtime:J

    .line 263
    iput-object p1, p0, Lcom/pairip/licensecheck/LicenseClient;->context:Landroid/content/Context;

    return-void
.end method

.method private bindToLicensingService(Z)V
    .locals 5
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x0
        }
        names = {
            "useBackgroundService"
        }
    .end annotation

    .line 357
    invoke-virtual {p0}, Lcom/pairip/licensecheck/LicenseClient;->unbindFromLicensingService()V

    if-eqz p1, :cond_0

    .line 361
    const-string v0, "Connecting to the background licensing service..."

    goto :goto_0

    .line 362
    :cond_0
    const-string v0, "Connecting to the main licensing service..."

    .line 358
    :goto_0
    const-string v1, "LicenseClient"

    invoke-static {v1, v0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    if-eqz p1, :cond_1

    .line 365
    const-string v0, "com.android.vending.licensing.IBackgroundLicensingService"

    goto :goto_1

    .line 366
    :cond_1
    const-string v0, "com.android.vending.licensing.ILicensingService"

    .line 367
    :goto_1
    new-instance v1, Landroid/content/Intent;

    invoke-direct {v1, v0}, Landroid/content/Intent;-><init>(Ljava/lang/String;)V

    const-string v2, "com.android.vending"

    .line 368
    invoke-virtual {v1, v2}, Landroid/content/Intent;->setPackage(Ljava/lang/String;)Landroid/content/Intent;

    move-result-object v1

    invoke-virtual {v1, v0}, Landroid/content/Intent;->setAction(Ljava/lang/String;)Landroid/content/Intent;

    move-result-object v1

    const/4 v2, 0x1

    if-nez p1, :cond_2

    .line 370
    invoke-static {}, Lcom/pairip/licensecheck/LicenseClient;->isLocalCheckPassed()Z

    move-result v3

    if-nez v3, :cond_2

    const/4 v3, 0x0

    goto :goto_2

    :cond_2
    const/4 v3, 0x1

    .line 372
    :goto_2
    :try_start_0
    iget-object v4, p0, Lcom/pairip/licensecheck/LicenseClient;->context:Landroid/content/Context;

    .line 373
    invoke-virtual {v4, v1, p0, v2}, Landroid/content/Context;->bindService(Landroid/content/Intent;Landroid/content/ServiceConnection;I)Z

    move-result v1
    :try_end_0
    .catch Ljava/lang/SecurityException; {:try_start_0 .. :try_end_0} :catch_0

    if-nez v1, :cond_3

    .line 383
    new-instance v1, Lcom/pairip/licensecheck/LicenseCheckException;

    const-string v2, "Could not bind with the licensing service: "

    invoke-virtual {v2, v0}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    invoke-direct {v1, v0}, Lcom/pairip/licensecheck/LicenseCheckException;-><init>(Ljava/lang/String;)V

    invoke-direct {p0, v1, v3, p1}, Lcom/pairip/licensecheck/LicenseClient;->retryOrThrow(Lcom/pairip/licensecheck/LicenseCheckException;ZZ)V

    goto :goto_3

    .line 389
    :cond_3
    iput-boolean v2, p0, Lcom/pairip/licensecheck/LicenseClient;->isBound:Z

    goto :goto_3

    :catch_0
    move-exception v1

    .line 375
    new-instance v2, Lcom/pairip/licensecheck/LicenseCheckException;

    const-string v4, "Not allowed to bind with the licensing service: "

    invoke-virtual {v4, v0}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    invoke-direct {v2, v0, v1}, Lcom/pairip/licensecheck/LicenseCheckException;-><init>(Ljava/lang/String;Ljava/lang/Throwable;)V

    invoke-direct {p0, v2, v3, p1}, Lcom/pairip/licensecheck/LicenseClient;->retryOrThrow(Lcom/pairip/licensecheck/LicenseCheckException;ZZ)V

    :goto_3
    return-void
.end method

.method public static checkLicense(Landroid/content/Context;)V
    .locals 0
    return-void
.end method

.method private checkLicenseInternal(Landroid/os/IBinder;)V
    .locals 6
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x0
        }
        names = {
            "licensingServiceBinder"
        }
    .end annotation

    .annotation system Ldalvik/annotation/Throws;
        value = {
            Lcom/pairip/licensecheck/LicenseCheckException;,
            Landroid/os/RemoteException;
        }
    .end annotation

    .line 456
    const-string v0, "Request to licensing service sent."

    if-nez p1, :cond_0

    .line 457
    new-instance p1, Lcom/pairip/licensecheck/LicenseCheckException;

    const-string v0, "Received a null binder."

    invoke-direct {p1, v0}, Lcom/pairip/licensecheck/LicenseCheckException;-><init>(Ljava/lang/String;)V

    invoke-direct {p0, p1}, Lcom/pairip/licensecheck/LicenseClient;->retryOrThrow(Lcom/pairip/licensecheck/LicenseCheckException;)V

    goto :goto_1

    .line 461
    :cond_0
    invoke-interface {p1}, Landroid/os/IBinder;->getInterfaceDescriptor()Ljava/lang/String;

    move-result-object v1

    const-string v2, "com.android.vending.licensing.IBackgroundLicensingService"

    .line 462
    invoke-virtual {v1, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v1

    if-nez v1, :cond_2

    .line 467
    const-string v1, "Sending request to licensing service..."

    const-string v2, "LicenseClient"

    invoke-static {v2, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 468
    invoke-static {}, Landroid/os/Parcel;->obtain()Landroid/os/Parcel;

    move-result-object v1

    .line 469
    invoke-static {}, Landroid/os/Parcel;->obtain()Landroid/os/Parcel;

    move-result-object v3

    .line 471
    :try_start_0
    invoke-direct {p0, v1, p1}, Lcom/pairip/licensecheck/LicenseClient;->populateInputDataForLicenseCheckV2(Landroid/os/Parcel;Landroid/os/IBinder;)V

    const/4 v4, 0x2

    const/4 v5, 0x0

    .line 473
    invoke-interface {p1, v4, v1, v3, v5}, Landroid/os/IBinder;->transact(ILandroid/os/Parcel;Landroid/os/Parcel;I)Z

    move-result p1

    if-nez p1, :cond_1

    .line 476
    new-instance p1, Lcom/pairip/licensecheck/LicenseCheckException;

    const-string v4, "Licensing service could not process request."

    invoke-direct {p1, v4}, Lcom/pairip/licensecheck/LicenseCheckException;-><init>(Ljava/lang/String;)V

    invoke-direct {p0, p1}, Lcom/pairip/licensecheck/LicenseClient;->handleError(Lcom/pairip/licensecheck/LicenseCheckException;)V
    :try_end_0
    .catch Landroid/os/DeadObjectException; {:try_start_0 .. :try_end_0} :catch_1
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    goto :goto_0

    :catch_0
    move-exception p1

    .line 481
    :try_start_1
    new-instance v4, Lcom/pairip/licensecheck/LicenseCheckException;

    const-string v5, "Error when calling licensing service."

    invoke-direct {v4, v5, p1}, Lcom/pairip/licensecheck/LicenseCheckException;-><init>(Ljava/lang/String;Ljava/lang/Throwable;)V

    invoke-direct {p0, v4}, Lcom/pairip/licensecheck/LicenseClient;->handleError(Lcom/pairip/licensecheck/LicenseCheckException;)V

    goto :goto_0

    :catch_1
    move-exception p1

    .line 479
    new-instance v4, Lcom/pairip/licensecheck/LicenseCheckException;

    const-string v5, "Licensing service process died."

    invoke-direct {v4, v5, p1}, Lcom/pairip/licensecheck/LicenseCheckException;-><init>(Ljava/lang/String;Ljava/lang/Throwable;)V

    invoke-direct {p0, v4}, Lcom/pairip/licensecheck/LicenseClient;->retryOrThrow(Lcom/pairip/licensecheck/LicenseCheckException;)V
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 483
    :cond_1
    :goto_0
    invoke-virtual {v1}, Landroid/os/Parcel;->recycle()V

    .line 484
    invoke-virtual {v3}, Landroid/os/Parcel;->recycle()V

    .line 485
    invoke-static {v2, v0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    :goto_1
    return-void

    :catchall_0
    move-exception p1

    .line 483
    invoke-virtual {v1}, Landroid/os/Parcel;->recycle()V

    .line 484
    invoke-virtual {v3}, Landroid/os/Parcel;->recycle()V

    .line 485
    invoke-static {v2, v0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 486
    throw p1

    .line 463
    :cond_2
    new-instance p1, Lcom/pairip/licensecheck/LicenseCheckException;

    const-string v0, "Background licensing service does not support full license check."

    invoke-direct {p1, v0}, Lcom/pairip/licensecheck/LicenseCheckException;-><init>(Ljava/lang/String;)V

    throw p1
.end method

.method static checkTrialStatus()Lcom/pairip/licensecheck/TrialStatus;
    .locals 2

    .line 204
    invoke-static {}, Lcom/pairip/licensecheck/LicenseClient;->isIsolatedProcess()Z

    move-result v0

    if-eqz v0, :cond_0

    .line 205
    const-string v0, "LicenseClient"

    const-string v1, "Skipping trial status check in isolated process."

    invoke-static {v0, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 206
    sget-object v0, Lcom/pairip/licensecheck/TrialStatus;->UNKNOWN:Lcom/pairip/licensecheck/TrialStatus;

    goto :goto_0

    .line 208
    :cond_0
    sget-object v0, Lcom/pairip/licensecheck/LicenseClient;->licenseCheckState:Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;

    invoke-virtual {v0}, Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;->ordinal()I

    move-result v0

    if-eqz v0, :cond_3

    const/4 v1, 0x1

    if-eq v0, v1, :cond_2

    const/4 v1, 0x2

    if-eq v0, v1, :cond_2

    const/4 v1, 0x3

    if-eq v0, v1, :cond_2

    const/4 v1, 0x4

    if-ne v0, v1, :cond_1

    .line 209
    sget-object v0, Lcom/pairip/licensecheck/TrialStatus;->UNDER_TRIAL:Lcom/pairip/licensecheck/TrialStatus;

    goto :goto_0

    .line 208
    :cond_1
    new-instance v0, Ljava/lang/RuntimeException;

    const/4 v1, 0x0

    invoke-direct {v0, v1, v1}, Ljava/lang/RuntimeException;-><init>(Ljava/lang/String;Ljava/lang/Throwable;)V

    throw v0

    .line 210
    :cond_2
    sget-object v0, Lcom/pairip/licensecheck/TrialStatus;->NOT_UNDER_TRIAL:Lcom/pairip/licensecheck/TrialStatus;

    goto :goto_0

    .line 211
    :cond_3
    sget-object v0, Lcom/pairip/licensecheck/TrialStatus;->UNKNOWN:Lcom/pairip/licensecheck/TrialStatus;

    :goto_0
    return-object v0
.end method

.method private createCloseAppIntentOrExitIfAppInBackground()Landroid/content/Intent;
    .locals 3

    .line 743
    invoke-direct {p0}, Lcom/pairip/licensecheck/LicenseClient;->isForeground()Z

    move-result v0

    if-nez v0, :cond_0

    .line 744
    sget-object v0, Lcom/pairip/licensecheck/LicenseClient;->exitAction:Ljava/lang/Runnable;

    invoke-interface {v0}, Ljava/lang/Runnable;->run()V

    .line 746
    :cond_0
    new-instance v0, Landroid/content/Intent;

    iget-object v1, p0, Lcom/pairip/licensecheck/LicenseClient;->context:Landroid/content/Context;

    const-class v2, Lcom/pairip/licensecheck/LicenseActivity;

    invoke-direct {v0, v1, v2}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V

    .line 747
    sget-boolean v1, Lcom/pairip/licensecheck/LicenseClient;->gracefulShutdownEnabled:Z

    if-eqz v1, :cond_1

    const/high16 v1, 0x10000

    .line 748
    invoke-virtual {v0, v1}, Landroid/content/Intent;->addFlags(I)Landroid/content/Intent;

    goto :goto_0

    :cond_1
    const/high16 v1, 0x4000000

    .line 750
    invoke-virtual {v0, v1}, Landroid/content/Intent;->addFlags(I)Landroid/content/Intent;

    const v1, 0x8000

    .line 751
    invoke-virtual {v0, v1}, Landroid/content/Intent;->addFlags(I)Landroid/content/Intent;

    :goto_0
    const/high16 v1, 0x10000000

    .line 753
    invoke-virtual {v0, v1}, Landroid/content/Intent;->addFlags(I)Landroid/content/Intent;

    return-object v0
.end method

.method private static createResultListener(Lcom/pairip/licensecheck/LicenseClient;)Lcom/pairip/licensecheck/ILicenseV2ResultListener;
    .locals 1
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x0
        }
        names = {
            "client"
        }
    .end annotation

    .line 572
    new-instance v0, Lcom/pairip/licensecheck/LicenseClient$2;

    invoke-direct {v0, p0}, Lcom/pairip/licensecheck/LicenseClient$2;-><init>(Lcom/pairip/licensecheck/LicenseClient;)V

    return-object v0
.end method

.method private static declared-synchronized getInstance(Landroid/content/Context;)Lcom/pairip/licensecheck/LicenseClient;
    .locals 2
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x0
        }
        names = {
            "context"
        }
    .end annotation

    const-class v0, Lcom/pairip/licensecheck/LicenseClient;

    monitor-enter v0

    .line 164
    :try_start_0
    sget-object v1, Lcom/pairip/licensecheck/LicenseClient;->instance:Lcom/pairip/licensecheck/LicenseClient;

    if-nez v1, :cond_0

    .line 165
    new-instance v1, Lcom/pairip/licensecheck/LicenseClient;

    invoke-direct {v1, p0}, Lcom/pairip/licensecheck/LicenseClient;-><init>(Landroid/content/Context;)V

    sput-object v1, Lcom/pairip/licensecheck/LicenseClient;->instance:Lcom/pairip/licensecheck/LicenseClient;
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 167
    :cond_0
    monitor-exit v0

    return-object v1

    :goto_0
    :try_start_1
    monitor-exit v0
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    throw p0

    :catchall_0
    move-exception p0

    goto :goto_0
.end method

.method public static getLicensePubKey()Ljava/lang/String;
    .locals 1

    .line 259
    sget-object v0, Lcom/pairip/licensecheck/LicenseClient;->licensePubKey:Ljava/lang/String;

    return-object v0
.end method

.method private handleError(Lcom/pairip/licensecheck/LicenseCheckException;)V
    .locals 2
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x0
        }
        names = {
            "ex"
        }
    .end annotation

    .line 713
    sget-object v0, Lcom/pairip/licensecheck/LicenseClient;->mainThreadRunner:Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;

    new-instance v1, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda4;

    invoke-direct {v1, p0, p1}, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda4;-><init>(Lcom/pairip/licensecheck/LicenseClient;Lcom/pairip/licensecheck/LicenseCheckException;)V

    invoke-interface {v0, v1}, Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;->run(Ljava/lang/Runnable;)V

    return-void
.end method

.method private initiateFreshLicensingServiceConnection(Z)V
    .locals 1
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x0
        }
        names = {
            "useBackgroundService"
        }
    .end annotation

    const/4 v0, 0x0

    .line 351
    iput v0, p0, Lcom/pairip/licensecheck/LicenseClient;->retryNum:I

    .line 352
    invoke-direct {p0, p1}, Lcom/pairip/licensecheck/LicenseClient;->bindToLicensingService(Z)V

    return-void
.end method

.method private isForeground()Z
    .locals 2

    .line 758
    new-instance v0, Landroid/app/ActivityManager$RunningAppProcessInfo;

    invoke-direct {v0}, Landroid/app/ActivityManager$RunningAppProcessInfo;-><init>()V

    .line 760
    invoke-static {v0}, Landroid/app/ActivityManager;->getMyMemoryState(Landroid/app/ActivityManager$RunningAppProcessInfo;)V

    .line 761
    iget v0, v0, Landroid/app/ActivityManager$RunningAppProcessInfo;->importance:I

    const/16 v1, 0x64

    if-gt v0, v1, :cond_0

    const/4 v0, 0x1

    goto :goto_0

    :cond_0
    const/4 v0, 0x0

    :goto_0
    return v0
.end method

.method private static isIsolatedProcess()Z
    .locals 2

    .line 244
    sget v0, Landroid/os/Build$VERSION;->SDK_INT:I

    const/16 v1, 0x1c

    if-lt v0, v1, :cond_0

    .line 245
    invoke-static {}, Landroid/os/Process;->isIsolated()Z

    move-result v0

    goto :goto_0

    .line 253
    :cond_0
    invoke-static {}, Landroid/os/Process;->myUid()I

    move-result v0

    const v1, 0x186a0

    .line 254
    rem-int/2addr v0, v1

    const v1, 0x182b8

    if-lt v0, v1, :cond_1

    const v1, 0x1869f

    if-gt v0, v1, :cond_1

    const/4 v0, 0x1

    goto :goto_0

    :cond_1
    const/4 v0, 0x0

    :goto_0
    return v0
.end method

.method private static isLocalCheckPassed()Z
    .locals 2

    .line 706
    sget-object v0, Lcom/pairip/licensecheck/LicenseClient;->licenseCheckState:Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;

    sget-object v1, Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;->LOCAL_CHECK_OK:Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;

    invoke-virtual {v0, v1}, Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;->equals(Ljava/lang/Object;)Z

    move-result v0

    if-nez v0, :cond_0

    sget-object v0, Lcom/pairip/licensecheck/LicenseClient;->licenseCheckState:Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;

    sget-object v1, Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;->LOCAL_CHECK_REPORTED:Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;

    .line 707
    invoke-virtual {v0, v1}, Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;->equals(Ljava/lang/Object;)Z

    move-result v0

    if-nez v0, :cond_0

    const/4 v0, 0x0

    goto :goto_0

    :cond_0
    const/4 v0, 0x1

    :goto_0
    return v0
.end method

.method static synthetic lambda$checkLicense$0(Landroid/content/Context;)V
    .locals 0

    .line 183
    invoke-static {p0}, Lcom/pairip/licensecheck/LicenseClient;->getInstance(Landroid/content/Context;)Lcom/pairip/licensecheck/LicenseClient;

    move-result-object p0

    invoke-virtual {p0}, Lcom/pairip/licensecheck/LicenseClient;->initializeLicenseCheck()V

    return-void
.end method

.method private synthetic lambda$handleError$0(Lcom/pairip/licensecheck/LicenseCheckException;)V
    .locals 2

    .line 715
    invoke-static {p1}, Landroid/util/Log;->getStackTraceString(Ljava/lang/Throwable;)Ljava/lang/String;

    move-result-object p1

    new-instance v0, Ljava/lang/StringBuilder;

    const-string v1, "Error while checking license: "

    invoke-direct {v0, v1}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    const-string v0, "LicenseClient"

    invoke-static {v0, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I

    .line 716
    invoke-virtual {p0}, Lcom/pairip/licensecheck/LicenseClient;->unbindFromLicensingService()V

    .line 717
    sget-object p1, Lcom/pairip/licensecheck/LicenseClient;->licenseCheckState:Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;

    sget-object v0, Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;->FULL_CHECK_OK:Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;

    invoke-virtual {p1, v0}, Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;->equals(Ljava/lang/Object;)Z

    move-result p1

    if-nez p1, :cond_0

    invoke-static {}, Lcom/pairip/licensecheck/LicenseClient;->isLocalCheckPassed()Z

    move-result p1

    if-nez p1, :cond_0

    .line 722
    invoke-direct {p0}, Lcom/pairip/licensecheck/LicenseClient;->startErrorDialogActivity()V

    :cond_0
    return-void
.end method

.method private synthetic lambda$initializeLicenseCheck$0()V
    .locals 3

    .line 274
    invoke-direct {p0}, Lcom/pairip/licensecheck/LicenseClient;->performLocalInstallerCheck()Z

    move-result v0

    .line 277
    sget-object v1, Lcom/pairip/licensecheck/LicenseClient;->mainThreadRunner:Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;

    new-instance v2, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda1;

    invoke-direct {v2, p0, v0}, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda1;-><init>(Lcom/pairip/licensecheck/LicenseClient;Z)V

    invoke-interface {v1, v2}, Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;->run(Ljava/lang/Runnable;)V

    return-void
.end method

.method private synthetic lambda$initializeLicenseCheck$1(Z)V
    .locals 0

    if-eqz p1, :cond_0

    .line 280
    sget-object p1, Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;->LOCAL_CHECK_OK:Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;

    sput-object p1, Lcom/pairip/licensecheck/LicenseClient;->licenseCheckState:Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;

    .line 284
    sget-boolean p1, Lcom/pairip/licensecheck/LicenseClient;->backgroundLicensingServiceEnabled:Z

    if-eqz p1, :cond_0

    const/4 p1, 0x1

    goto :goto_0

    :cond_0
    const/4 p1, 0x0

    .line 286
    :goto_0
    invoke-direct {p0, p1}, Lcom/pairip/licensecheck/LicenseClient;->initiateFreshLicensingServiceConnection(Z)V

    return-void
.end method

.method private synthetic lambda$onServiceConnected$0(Landroid/os/IBinder;)V
    .locals 2

    .line 408
    :try_start_0
    invoke-direct {p0, p1}, Lcom/pairip/licensecheck/LicenseClient;->checkLicenseInternal(Landroid/os/IBinder;)V
    :try_end_0
    .catch Lcom/pairip/licensecheck/LicenseCheckException; {:try_start_0 .. :try_end_0} :catch_1
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    :catch_0
    move-exception p1

    .line 412
    new-instance v0, Lcom/pairip/licensecheck/LicenseCheckException;

    const-string v1, "Error when getting interface descriptor."

    invoke-direct {v0, v1, p1}, Lcom/pairip/licensecheck/LicenseCheckException;-><init>(Ljava/lang/String;Ljava/lang/Throwable;)V

    invoke-direct {p0, v0}, Lcom/pairip/licensecheck/LicenseClient;->handleError(Lcom/pairip/licensecheck/LicenseCheckException;)V

    goto :goto_0

    :catch_1
    move-exception p1

    .line 410
    invoke-direct {p0, p1}, Lcom/pairip/licensecheck/LicenseClient;->handleError(Lcom/pairip/licensecheck/LicenseCheckException;)V

    :goto_0
    return-void
.end method

.method private synthetic lambda$onServiceConnected$1(Landroid/os/IBinder;)V
    .locals 2

    .line 422
    :try_start_0
    invoke-virtual {p0, p1}, Lcom/pairip/licensecheck/LicenseClient;->reportSuccessfulLicenseCheck(Landroid/os/IBinder;)V
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    :catch_0
    move-exception p1

    .line 424
    invoke-static {p1}, Landroid/util/Log;->getStackTraceString(Ljava/lang/Throwable;)Ljava/lang/String;

    move-result-object p1

    new-instance v0, Ljava/lang/StringBuilder;

    const-string v1, "Error while reporting license check: "

    invoke-direct {v0, v1}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    const-string v0, "LicenseClient"

    invoke-static {v0, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I

    .line 425
    sget-object p1, Lcom/pairip/licensecheck/LicenseClient;->mainThreadRunner:Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;

    new-instance v0, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda0;

    invoke-direct {v0, p0}, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda0;-><init>(Lcom/pairip/licensecheck/LicenseClient;)V

    invoke-interface {p1, v0}, Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;->run(Ljava/lang/Runnable;)V

    :goto_0
    return-void
.end method

.method private synthetic lambda$processResponse$0(Lcom/pairip/licensecheck/RepeatedCheckMetadata;Landroid/os/Bundle;)V
    .locals 2

    if-eqz p1, :cond_0

    .line 630
    sget-object v0, Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;->REPEATED_CHECK_REQUIRED:Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;

    sput-object v0, Lcom/pairip/licensecheck/LicenseClient;->licenseCheckState:Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;

    .line 631
    invoke-virtual {p0}, Lcom/pairip/licensecheck/LicenseClient;->getElapsedRealtimeMillis()J

    move-result-wide v0

    iput-wide v0, p0, Lcom/pairip/licensecheck/LicenseClient;->repeatedCheckStartElapsedRealtime:J

    .line 632
    invoke-direct {p0, p1}, Lcom/pairip/licensecheck/LicenseClient;->scheduleRepeatedLicenseCheck(Lcom/pairip/licensecheck/RepeatedCheckMetadata;)V

    goto :goto_0

    .line 634
    :cond_0
    sget-object p1, Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;->FULL_CHECK_OK:Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;

    sput-object p1, Lcom/pairip/licensecheck/LicenseClient;->licenseCheckState:Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;

    .line 636
    :goto_0
    sput-object p2, Lcom/pairip/licensecheck/LicenseClient;->responsePayload:Landroid/os/Bundle;

    return-void
.end method

.method static synthetic lambda$reportSuccessfulLicenseCheck$0()V
    .locals 1

    .line 515
    sget-object v0, Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;->LOCAL_CHECK_REPORTED:Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;

    sput-object v0, Lcom/pairip/licensecheck/LicenseClient;->licenseCheckState:Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;

    return-void
.end method

.method private synthetic lambda$retryOrThrow$0(ZLcom/pairip/licensecheck/LicenseCheckException;Z)V
    .locals 6

    .line 595
    invoke-virtual {p0}, Lcom/pairip/licensecheck/LicenseClient;->unbindFromLicensingService()V

    .line 596
    iget v0, p0, Lcom/pairip/licensecheck/LicenseClient;->retryNum:I

    const-string v1, "LicenseClient"

    const/4 v2, 0x3

    if-ge v0, v2, :cond_1

    const/4 p3, 0x1

    add-int/2addr v0, p3

    .line 597
    iput v0, p0, Lcom/pairip/licensecheck/LicenseClient;->retryNum:I

    .line 599
    iget-object v0, p0, Lcom/pairip/licensecheck/LicenseClient;->delayedTaskExecutor:Lcom/pairip/licensecheck/LicenseClient$DelayedTaskExecutor;

    new-instance v3, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda13;

    invoke-direct {v3, p0, p1}, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda13;-><init>(Lcom/pairip/licensecheck/LicenseClient;Z)V

    const-wide/16 v4, 0x7d0

    invoke-interface {v0, v3, v4, v5}, Lcom/pairip/licensecheck/LicenseClient$DelayedTaskExecutor;->schedule(Ljava/lang/Runnable;J)V

    .line 600
    iget p1, p0, Lcom/pairip/licensecheck/LicenseClient;->retryNum:I

    .line 604
    invoke-static {p1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object p1

    if-nez p2, :cond_0

    const-string p2, "null"

    goto :goto_0

    :cond_0
    invoke-virtual {p2}, Lcom/pairip/licensecheck/LicenseCheckException;->getMessage()Ljava/lang/String;

    move-result-object p2

    :goto_0
    const-wide/16 v3, 0x2

    invoke-static {v3, v4}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;

    move-result-object v0

    new-array v2, v2, [Ljava/lang/Object;

    const/4 v3, 0x0

    aput-object p1, v2, v3

    aput-object p2, v2, p3

    const/4 p1, 0x2

    aput-object v0, v2, p1

    .line 602
    const-string p1, "Retry #%d. License check failed with error \'%s\'. Next try in %ds..."

    invoke-static {p1, v2}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object p1

    .line 600
    invoke-static {v1, p1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_1

    :cond_1
    if-eqz p3, :cond_2

    .line 607
    invoke-static {p2}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object p1

    new-instance p2, Ljava/lang/StringBuilder;

    const-string p3, "Retry limit reached for: "

    invoke-direct {p2, p3}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {p2, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-static {v1, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_1

    .line 609
    :cond_2
    invoke-direct {p0, p2}, Lcom/pairip/licensecheck/LicenseClient;->handleError(Lcom/pairip/licensecheck/LicenseCheckException;)V

    :goto_1
    return-void
.end method

.method private synthetic lambda$retryOrThrow$1(Z)V
    .locals 0

    .line 599
    invoke-direct {p0, p1}, Lcom/pairip/licensecheck/LicenseClient;->bindToLicensingService(Z)V

    return-void
.end method

.method private synthetic lambda$scheduleRepeatedLicenseCheck$0(Lcom/pairip/licensecheck/RepeatedCheckMetadata;)V
    .locals 7

    .line 665
    invoke-virtual {p0}, Lcom/pairip/licensecheck/LicenseClient;->getElapsedRealtimeMillis()J

    move-result-wide v0

    .line 666
    iget-wide v2, p0, Lcom/pairip/licensecheck/LicenseClient;->repeatedCheckStartElapsedRealtime:J

    sub-long/2addr v0, v2

    .line 668
    invoke-virtual {p0}, Lcom/pairip/licensecheck/LicenseClient;->getCurrentTimeMillis()J

    move-result-wide v2

    .line 669
    invoke-virtual {p1}, Lcom/pairip/licensecheck/RepeatedCheckMetadata;->getTimeToRetryMillis()J

    move-result-wide v4

    cmp-long v6, v2, v4

    if-gez v6, :cond_0

    .line 670
    invoke-virtual {p1}, Lcom/pairip/licensecheck/RepeatedCheckMetadata;->getDurationToRetryMillis()J

    move-result-wide v2

    cmp-long v4, v0, v2

    if-gez v4, :cond_0

    .line 674
    const-string v0, "LicenseClient"

    const-string v1, "Repeated license check is rescheduled."

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 675
    invoke-direct {p0, p1}, Lcom/pairip/licensecheck/LicenseClient;->scheduleRepeatedLicenseCheck(Lcom/pairip/licensecheck/RepeatedCheckMetadata;)V

    goto :goto_0

    :cond_0
    const/4 p1, 0x0

    .line 671
    iput-boolean p1, p0, Lcom/pairip/licensecheck/LicenseClient;->waitingForRepeatedCheck:Z

    .line 672
    invoke-direct {p0, p1}, Lcom/pairip/licensecheck/LicenseClient;->initiateFreshLicensingServiceConnection(Z)V

    :goto_0
    return-void
.end method

.method static synthetic lambda$static$0(Ljava/lang/Runnable;)V
    .locals 1

    .line 121
    new-instance v0, Ljava/lang/Thread;

    invoke-direct {v0, p0}, Ljava/lang/Thread;-><init>(Ljava/lang/Runnable;)V

    invoke-virtual {v0}, Ljava/lang/Thread;->start()V

    return-void
.end method

.method static synthetic lambda$stopTrial$0(Landroid/content/Context;)V
    .locals 0

    .line 196
    invoke-static {p0}, Lcom/pairip/licensecheck/LicenseClient;->getInstance(Landroid/content/Context;)Lcom/pairip/licensecheck/LicenseClient;

    move-result-object p0

    invoke-virtual {p0}, Lcom/pairip/licensecheck/LicenseClient;->handleTrialEnd()V

    return-void
.end method

.method private performLocalInstallerCheck()Z
    .locals 6

    .line 312
    const-string v0, "LicenseClient"

    const/4 v1, 0x0

    :try_start_0
    sget v2, Landroid/os/Build$VERSION;->SDK_INT:I

    const/16 v3, 0x1e

    if-ge v2, v3, :cond_0

    .line 313
    const-string v2, "Local install check bypassed due to old SDK version."

    invoke-static {v0, v2}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_0

    .line 316
    :cond_0
    iget-object v2, p0, Lcom/pairip/licensecheck/LicenseClient;->context:Landroid/content/Context;

    invoke-virtual {v2}, Landroid/content/Context;->getPackageManager()Landroid/content/pm/PackageManager;

    move-result-object v2

    if-nez v2, :cond_1

    .line 318
    const-string v2, "Local install check bypassed due to package manager not found."

    invoke-static {v0, v2}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_0

    .line 321
    :cond_1
    sget-object v3, Lcom/pairip/licensecheck/LicenseClient;->packageName:Ljava/lang/String;

    invoke-virtual {v2, v3, v1}, Landroid/content/pm/PackageManager;->getPackageInfo(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;

    move-result-object v3

    if-eqz v3, :cond_6

    .line 322
    iget-object v4, v3, Landroid/content/pm/PackageInfo;->applicationInfo:Landroid/content/pm/ApplicationInfo;

    if-eqz v4, :cond_6

    .line 326
    iget-object v3, v3, Landroid/content/pm/PackageInfo;->applicationInfo:Landroid/content/pm/ApplicationInfo;

    iget v3, v3, Landroid/content/pm/ApplicationInfo;->flags:I

    and-int/lit8 v4, v3, 0x1

    if-nez v4, :cond_4

    and-int/lit16 v3, v3, 0x80

    if-nez v3, :cond_4

    .line 332
    sget-object v3, Lcom/pairip/licensecheck/LicenseClient;->packageName:Ljava/lang/String;

    invoke-virtual {v2, v3}, Landroid/content/pm/PackageManager;->getInstallSourceInfo(Ljava/lang/String;)Landroid/content/pm/InstallSourceInfo;

    move-result-object v2

    if-nez v2, :cond_2

    .line 334
    const-string v2, "Local install check bypassed due to install source info not found."

    invoke-static {v0, v2}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_0

    .line 337
    :cond_2
    invoke-virtual {v2}, Landroid/content/pm/InstallSourceInfo;->getInstallingPackageName()Ljava/lang/String;

    move-result-object v2

    if-eqz v2, :cond_3

    .line 338
    const-string v3, "com.android.vending"

    invoke-virtual {v2, v3}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v2

    if-nez v2, :cond_5

    .line 339
    :cond_3
    const-string v2, "Local install check failed due to wrong installer."

    invoke-static {v0, v2}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_0

    .line 329
    :cond_4
    const-string v2, "Local install check passed due to system app."

    invoke-static {v0, v2}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    :cond_5
    const/4 v1, 0x1

    goto :goto_0

    .line 323
    :cond_6
    const-string v2, "Local install check bypassed due to app package info not found."

    invoke-static {v0, v2}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    :catch_0
    move-exception v2

    .line 344
    const-string v3, "Could not obtain package info for local installer check."

    invoke-static {v0, v3, v2}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    :goto_0
    return v1
.end method

.method private populateInputDataForLicenseCheckV2(Landroid/os/Parcel;Landroid/os/IBinder;)V
    .locals 3
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x0,
            0x0
        }
        names = {
            "inputData",
            "licensingService"
        }
    .end annotation

    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    .line 540
    invoke-interface {p2}, Landroid/os/IBinder;->getInterfaceDescriptor()Ljava/lang/String;

    move-result-object p2

    invoke-virtual {p1, p2}, Landroid/os/Parcel;->writeInterfaceToken(Ljava/lang/String;)V

    .line 541
    sget-object p2, Lcom/pairip/licensecheck/LicenseClient;->packageName:Ljava/lang/String;

    invoke-virtual {p1, p2}, Landroid/os/Parcel;->writeString(Ljava/lang/String;)V

    .line 542
    invoke-static {p0}, Lcom/pairip/licensecheck/LicenseClient;->createResultListener(Lcom/pairip/licensecheck/LicenseClient;)Lcom/pairip/licensecheck/ILicenseV2ResultListener;

    move-result-object p2

    invoke-interface {p2}, Lcom/pairip/licensecheck/ILicenseV2ResultListener;->asBinder()Landroid/os/IBinder;

    move-result-object p2

    invoke-virtual {p1, p2}, Landroid/os/Parcel;->writeStrongBinder(Landroid/os/IBinder;)V

    .line 544
    new-instance p2, Landroid/os/Bundle;

    invoke-direct {p2}, Landroid/os/Bundle;-><init>()V

    .line 545
    sget-boolean v0, Lcom/pairip/licensecheck/LicenseClient;->customTrialEndTriggered:Z

    const/4 v1, 0x1

    if-eqz v0, :cond_0

    .line 546
    const-string v0, "end_custom_trial"

    invoke-virtual {p2, v0, v1}, Landroid/os/Bundle;->putBoolean(Ljava/lang/String;Z)V

    .line 548
    :cond_0
    invoke-virtual {p2}, Landroid/os/Bundle;->isEmpty()Z

    move-result v0

    const/4 v2, 0x0

    if-nez v0, :cond_1

    .line 550
    invoke-virtual {p1, v1}, Landroid/os/Parcel;->writeInt(I)V

    .line 551
    invoke-virtual {p2, p1, v2}, Landroid/os/Bundle;->writeToParcel(Landroid/os/Parcel;I)V

    goto :goto_0

    .line 554
    :cond_1
    invoke-virtual {p1, v2}, Landroid/os/Parcel;->writeInt(I)V

    :goto_0
    return-void
.end method

.method private populateInputDataForReportAutoVerifiedLicense(Landroid/os/Parcel;Landroid/os/IBinder;)V
    .locals 0
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x0,
            0x0
        }
        names = {
            "inputData",
            "licensingService"
        }
    .end annotation

    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    .line 565
    invoke-interface {p2}, Landroid/os/IBinder;->getInterfaceDescriptor()Ljava/lang/String;

    move-result-object p2

    invoke-virtual {p1, p2}, Landroid/os/Parcel;->writeInterfaceToken(Ljava/lang/String;)V

    .line 566
    sget-object p2, Lcom/pairip/licensecheck/LicenseClient;->packageName:Ljava/lang/String;

    invoke-virtual {p1, p2}, Landroid/os/Parcel;->writeString(Ljava/lang/String;)V

    const/4 p2, 0x0

    .line 568
    invoke-virtual {p1, p2}, Landroid/os/Parcel;->writeInt(I)V

    return-void
.end method

.method private processResponse(ILandroid/os/Bundle;)V
    .locals 3
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x0,
            0x0
        }
        names = {
            "responseCode",
            "responsePayload"
        }
    .end annotation

    const/4 v0, 0x3

    if-eq p1, v0, :cond_3

    if-nez p1, :cond_1

    .line 621
    :try_start_0
    sget-object p1, Lcom/pairip/licensecheck/LicenseClient;->packageName:Ljava/lang/String;

    invoke-static {p2, p1}, Lcom/pairip/licensecheck/LicenseResponseHelper;->validateResponse(Landroid/os/Bundle;Ljava/lang/String;)V

    .line 622
    const-string p1, "LicenseClient"

    const-string v0, "License check succeeded."

    invoke-static {p1, v0}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 624
    sget-boolean p1, Lcom/pairip/licensecheck/LicenseClient;->repeatedCheckEnabled:Z

    if-eqz p1, :cond_0

    .line 625
    invoke-static {p2}, Lcom/pairip/licensecheck/LicenseResponseHelper;->getRepeatedCheckMetadata(Landroid/os/Bundle;)Lcom/pairip/licensecheck/RepeatedCheckMetadata;

    move-result-object p1

    goto :goto_0

    :cond_0
    const/4 p1, 0x0

    .line 627
    :goto_0
    sget-object v0, Lcom/pairip/licensecheck/LicenseClient;->mainThreadRunner:Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;

    new-instance v1, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda11;

    invoke-direct {v1, p0, p1, p2}, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda11;-><init>(Lcom/pairip/licensecheck/LicenseClient;Lcom/pairip/licensecheck/RepeatedCheckMetadata;Landroid/os/Bundle;)V

    invoke-interface {v0, v1}, Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;->run(Ljava/lang/Runnable;)V

    goto :goto_1

    :cond_1
    const/4 v0, 0x2

    if-ne p1, v0, :cond_2

    .line 639
    const-string p1, "PAYWALL_INTENT"

    invoke-virtual {p2, p1}, Landroid/os/Bundle;->getParcelable(Ljava/lang/String;)Landroid/os/Parcelable;

    move-result-object p1

    check-cast p1, Landroid/app/PendingIntent;

    .line 640
    invoke-direct {p0, p1}, Lcom/pairip/licensecheck/LicenseClient;->startPaywallActivity(Landroid/app/PendingIntent;)V
    :try_end_0
    .catch Lcom/pairip/licensecheck/LicenseCheckException; {:try_start_0 .. :try_end_0} :catch_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 648
    :goto_1
    sget-object p1, Lcom/pairip/licensecheck/LicenseClient;->mainThreadRunner:Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;

    new-instance p2, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda0;

    invoke-direct {p2, p0}, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda0;-><init>(Lcom/pairip/licensecheck/LicenseClient;)V

    goto :goto_2

    .line 642
    :cond_2
    :try_start_1
    new-instance p2, Lcom/pairip/licensecheck/LicenseCheckException;

    const-string v0, "Unexpected response code %d received."

    .line 643
    invoke-static {p1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object p1

    const/4 v1, 0x1

    new-array v1, v1, [Ljava/lang/Object;

    const/4 v2, 0x0

    aput-object p1, v1, v2

    invoke-static {v0, v1}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object p1

    invoke-direct {p2, p1}, Lcom/pairip/licensecheck/LicenseCheckException;-><init>(Ljava/lang/String;)V

    throw p2

    .line 619
    :cond_3
    new-instance p1, Lcom/pairip/licensecheck/LicenseCheckException;

    const-string p2, "Request package name invalid."

    invoke-direct {p1, p2}, Lcom/pairip/licensecheck/LicenseCheckException;-><init>(Ljava/lang/String;)V

    throw p1
    :try_end_1
    .catch Lcom/pairip/licensecheck/LicenseCheckException; {:try_start_1 .. :try_end_1} :catch_0
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    :catch_0
    move-exception p1

    .line 646
    :try_start_2
    invoke-direct {p0, p1}, Lcom/pairip/licensecheck/LicenseClient;->handleError(Lcom/pairip/licensecheck/LicenseCheckException;)V
    :try_end_2
    .catchall {:try_start_2 .. :try_end_2} :catchall_0

    .line 648
    sget-object p1, Lcom/pairip/licensecheck/LicenseClient;->mainThreadRunner:Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;

    new-instance p2, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda0;

    invoke-direct {p2, p0}, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda0;-><init>(Lcom/pairip/licensecheck/LicenseClient;)V

    :goto_2
    invoke-interface {p1, p2}, Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;->run(Ljava/lang/Runnable;)V

    return-void

    :catchall_0
    move-exception p1

    sget-object p2, Lcom/pairip/licensecheck/LicenseClient;->mainThreadRunner:Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;

    new-instance v0, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda0;

    invoke-direct {v0, p0}, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda0;-><init>(Lcom/pairip/licensecheck/LicenseClient;)V

    invoke-interface {p2, v0}, Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;->run(Ljava/lang/Runnable;)V

    .line 649
    throw p1
.end method

.method private retryOrThrow(Lcom/pairip/licensecheck/LicenseCheckException;)V
    .locals 1
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x0
        }
        names = {
            "error"
        }
    .end annotation

    const/4 v0, 0x0

    .line 581
    invoke-direct {p0, p1, v0, v0}, Lcom/pairip/licensecheck/LicenseClient;->retryOrThrow(Lcom/pairip/licensecheck/LicenseCheckException;ZZ)V

    return-void
.end method

.method private retryOrThrow(Lcom/pairip/licensecheck/LicenseCheckException;ZZ)V
    .locals 2
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x0,
            0x0,
            0x0
        }
        names = {
            "error",
            "ignoreErrorOnFinalFailure",
            "useBackgroundService"
        }
    .end annotation

    .line 593
    sget-object v0, Lcom/pairip/licensecheck/LicenseClient;->mainThreadRunner:Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;

    new-instance v1, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda2;

    invoke-direct {v1, p0, p3, p1, p2}, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda2;-><init>(Lcom/pairip/licensecheck/LicenseClient;ZLcom/pairip/licensecheck/LicenseCheckException;Z)V

    invoke-interface {v0, v1}, Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;->run(Ljava/lang/Runnable;)V

    return-void
.end method

.method private scheduleAppShutdown()V
    .locals 4

    .line 775
    sget-boolean v0, Lcom/pairip/licensecheck/LicenseClient;->eventualShutdownEnabled:Z

    if-eqz v0, :cond_0

    .line 776
    iget-object v0, p0, Lcom/pairip/licensecheck/LicenseClient;->delayedTaskExecutor:Lcom/pairip/licensecheck/LicenseClient$DelayedTaskExecutor;

    sget-object v1, Lcom/pairip/licensecheck/LicenseClient;->exitAction:Ljava/lang/Runnable;

    const-wide/16 v2, 0x7530

    invoke-interface {v0, v1, v2, v3}, Lcom/pairip/licensecheck/LicenseClient$DelayedTaskExecutor;->schedule(Ljava/lang/Runnable;J)V

    :cond_0
    return-void
.end method

.method private scheduleRepeatedLicenseCheck(Lcom/pairip/licensecheck/RepeatedCheckMetadata;)V
    .locals 6
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x0
        }
        names = {
            "repeatedCheckMetadata"
        }
    .end annotation

    .line 653
    invoke-virtual {p0}, Lcom/pairip/licensecheck/LicenseClient;->getCurrentTimeMillis()J

    move-result-wide v0

    .line 657
    invoke-virtual {p1}, Lcom/pairip/licensecheck/RepeatedCheckMetadata;->getDurationToRetryMillis()J

    move-result-wide v2

    .line 658
    invoke-virtual {p1}, Lcom/pairip/licensecheck/RepeatedCheckMetadata;->getTimeToRetryMillis()J

    move-result-wide v4

    sub-long/2addr v4, v0

    const-wide/16 v0, 0x0

    invoke-static {v0, v1, v4, v5}, Ljava/lang/Math;->max(JJ)J

    move-result-wide v0

    .line 656
    invoke-static {v2, v3, v0, v1}, Ljava/lang/Math;->min(JJ)J

    move-result-wide v0

    const-wide/32 v2, 0x493e0

    .line 655
    invoke-static {v0, v1, v2, v3}, Ljava/lang/Math;->min(JJ)J

    move-result-wide v0

    .line 660
    iget-boolean v2, p0, Lcom/pairip/licensecheck/LicenseClient;->waitingForRepeatedCheck:Z

    const/4 v3, 0x1

    if-nez v2, :cond_0

    .line 661
    iput-boolean v3, p0, Lcom/pairip/licensecheck/LicenseClient;->waitingForRepeatedCheck:Z

    .line 663
    :cond_0
    iget-object v2, p0, Lcom/pairip/licensecheck/LicenseClient;->delayedTaskExecutor:Lcom/pairip/licensecheck/LicenseClient$DelayedTaskExecutor;

    new-instance v4, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda14;

    invoke-direct {v4, p0, p1}, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda14;-><init>(Lcom/pairip/licensecheck/LicenseClient;Lcom/pairip/licensecheck/RepeatedCheckMetadata;)V

    invoke-interface {v2, v4, v0, v1}, Lcom/pairip/licensecheck/LicenseClient$DelayedTaskExecutor;->schedule(Ljava/lang/Runnable;J)V

    .line 679
    invoke-static {v0, v1}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;

    move-result-object p1

    new-array v0, v3, [Ljava/lang/Object;

    const/4 v1, 0x0

    aput-object p1, v0, v1

    const-string p1, "Repeated license check is scheduled in %d ms..."

    invoke-static {p1, v0}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object p1

    const-string v0, "LicenseClient"

    invoke-static {v0, p1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    return-void
.end method

.method private startErrorDialogActivity()V
    .locals 3

    .line 735
    invoke-direct {p0}, Lcom/pairip/licensecheck/LicenseClient;->createCloseAppIntentOrExitIfAppInBackground()Landroid/content/Intent;

    move-result-object v0

    .line 736
    const-string v1, "activitytype"

    sget-object v2, Lcom/pairip/licensecheck/LicenseActivity$ActivityType;->ERROR_DIALOG:Lcom/pairip/licensecheck/LicenseActivity$ActivityType;

    invoke-virtual {v0, v1, v2}, Landroid/content/Intent;->putExtra(Ljava/lang/String;Ljava/io/Serializable;)Landroid/content/Intent;

    .line 738
    invoke-direct {p0}, Lcom/pairip/licensecheck/LicenseClient;->scheduleAppShutdown()V

    .line 739
    iget-object v1, p0, Lcom/pairip/licensecheck/LicenseClient;->context:Landroid/content/Context;

    invoke-virtual {v1, v0}, Landroid/content/Context;->startActivity(Landroid/content/Intent;)V

    return-void
.end method

.method private startPaywallActivity(Landroid/app/PendingIntent;)V
    .locals 2
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x0
        }
        names = {
            "paywallIntent"
        }
    .end annotation

    .line 727
    invoke-direct {p0}, Lcom/pairip/licensecheck/LicenseClient;->createCloseAppIntentOrExitIfAppInBackground()Landroid/content/Intent;

    move-result-object v0

    .line 728
    const-string v1, "paywallintent"

    invoke-virtual {v0, v1, p1}, Landroid/content/Intent;->putExtra(Ljava/lang/String;Landroid/os/Parcelable;)Landroid/content/Intent;

    .line 729
    const-string p1, "activitytype"

    sget-object v1, Lcom/pairip/licensecheck/LicenseActivity$ActivityType;->PAYWALL:Lcom/pairip/licensecheck/LicenseActivity$ActivityType;

    invoke-virtual {v0, p1, v1}, Landroid/content/Intent;->putExtra(Ljava/lang/String;Ljava/io/Serializable;)Landroid/content/Intent;

    .line 730
    invoke-direct {p0}, Lcom/pairip/licensecheck/LicenseClient;->scheduleAppShutdown()V

    .line 731
    iget-object p1, p0, Lcom/pairip/licensecheck/LicenseClient;->context:Landroid/content/Context;

    invoke-virtual {p1, v0}, Landroid/content/Context;->startActivity(Landroid/content/Intent;)V

    return-void
.end method

.method static stopTrial(Landroid/content/Context;)V
    .locals 2
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x0
        }
        names = {
            "context"
        }
    .end annotation

    .line 188
    const-string v0, "LicenseClient"

    if-nez p0, :cond_0

    .line 189
    const-string p0, "Cannot trigger trial end with null context."

    invoke-static {v0, p0}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_0

    .line 192
    :cond_0
    invoke-static {}, Lcom/pairip/licensecheck/LicenseClient;->isIsolatedProcess()Z

    move-result v1

    if-eqz v1, :cond_1

    .line 193
    const-string p0, "Skipping trial end in isolated process."

    invoke-static {v0, p0}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_0

    .line 196
    :cond_1
    sget-object v0, Lcom/pairip/licensecheck/LicenseClient;->mainThreadRunner:Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;

    new-instance v1, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda3;

    invoke-direct {v1, p0}, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda3;-><init>(Landroid/content/Context;)V

    invoke-interface {v0, v1}, Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;->run(Ljava/lang/Runnable;)V

    :goto_0
    return-void
.end method


# virtual methods
.method protected getCurrentTimeMillis()J
    .locals 2

    .line 766
    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J

    move-result-wide v0

    return-wide v0
.end method

.method protected getElapsedRealtimeMillis()J
    .locals 2

    .line 771
    invoke-static {}, Landroid/os/SystemClock;->elapsedRealtime()J

    move-result-wide v0

    return-wide v0
.end method

.method protected handleTrialEnd()V
    .locals 12

    .line 221
    invoke-virtual {p0}, Lcom/pairip/licensecheck/LicenseClient;->getElapsedRealtimeMillis()J

    move-result-wide v0

    .line 222
    sget-wide v2, Lcom/pairip/licensecheck/LicenseClient;->lastTrialEndElapsedRealtimeMillis:J

    sub-long v4, v0, v2

    const-wide/16 v6, 0x0

    const/4 v8, 0x1

    .line 223
    const-string v9, "LicenseClient"

    const/4 v10, 0x0

    cmp-long v11, v2, v6

    if-lez v11, :cond_0

    const-wide/16 v2, 0xbb8

    cmp-long v6, v4, v2

    if-gez v6, :cond_0

    .line 230
    invoke-static {v4, v5}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;

    move-result-object v0

    new-array v1, v8, [Ljava/lang/Object;

    aput-object v0, v1, v10

    .line 229
    const-string v0, "Trial end trigger throttled. Ignoring request (sent %d ms ago)."

    invoke-static {v0, v1}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v0

    .line 227
    invoke-static {v9, v0}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_0

    .line 234
    :cond_0
    const-string v2, "Trial end event triggered; initiating full license check."

    invoke-static {v9, v2}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 235
    sput-wide v0, Lcom/pairip/licensecheck/LicenseClient;->lastTrialEndElapsedRealtimeMillis:J

    .line 236
    sput-boolean v8, Lcom/pairip/licensecheck/LicenseClient;->customTrialEndTriggered:Z

    .line 237
    sget-object v0, Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;->CHECK_REQUIRED:Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;

    sput-object v0, Lcom/pairip/licensecheck/LicenseClient;->licenseCheckState:Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;

    .line 238
    iput-boolean v10, p0, Lcom/pairip/licensecheck/LicenseClient;->waitingForRepeatedCheck:Z

    .line 240
    invoke-direct {p0, v10}, Lcom/pairip/licensecheck/LicenseClient;->initiateFreshLicensingServiceConnection(Z)V

    :goto_0
    return-void
.end method

.method public initializeLicenseCheck()V
    .locals 3

    .line 268
    sget-object v0, Lcom/pairip/licensecheck/LicenseClient;->licenseCheckState:Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;

    invoke-virtual {v0}, Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;->ordinal()I

    move-result v0

    const/4 v1, 0x0

    if-eqz v0, :cond_1

    const/4 v2, 0x1

    if-eq v0, v2, :cond_0

    const/4 v2, 0x4

    if-ne v0, v2, :cond_3

    .line 304
    invoke-direct {p0, v1}, Lcom/pairip/licensecheck/LicenseClient;->initiateFreshLicensingServiceConnection(Z)V

    goto :goto_0

    .line 295
    :cond_0
    :try_start_0
    sget-object v0, Lcom/pairip/licensecheck/LicenseClient;->responsePayload:Landroid/os/Bundle;

    sget-object v1, Lcom/pairip/licensecheck/LicenseClient;->packageName:Ljava/lang/String;

    invoke-static {v0, v1}, Lcom/pairip/licensecheck/LicenseResponseHelper;->validateResponse(Landroid/os/Bundle;Ljava/lang/String;)V
    :try_end_0
    .catch Lcom/pairip/licensecheck/LicenseCheckException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    :catch_0
    move-exception v0

    .line 297
    invoke-direct {p0, v0}, Lcom/pairip/licensecheck/LicenseClient;->handleError(Lcom/pairip/licensecheck/LicenseCheckException;)V

    goto :goto_0

    .line 270
    :cond_1
    sget-boolean v0, Lcom/pairip/licensecheck/LicenseClient;->localCheckEnabled:Z

    if-eqz v0, :cond_2

    sget-boolean v0, Lcom/pairip/licensecheck/LicenseClient;->customTrialEndTriggered:Z

    if-nez v0, :cond_2

    .line 272
    sget-object v0, Lcom/pairip/licensecheck/LicenseClient;->backgroundRunner:Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;

    new-instance v1, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda8;

    invoke-direct {v1, p0}, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda8;-><init>(Lcom/pairip/licensecheck/LicenseClient;)V

    invoke-interface {v0, v1}, Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;->run(Ljava/lang/Runnable;)V

    goto :goto_0

    .line 290
    :cond_2
    invoke-direct {p0, v1}, Lcom/pairip/licensecheck/LicenseClient;->initiateFreshLicensingServiceConnection(Z)V

    :cond_3
    :goto_0
    return-void
.end method

.method public onServiceConnected(Landroid/content/ComponentName;Landroid/os/IBinder;)V
    .locals 1
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x0,
            0x0
        }
        names = {
            "componentName",
            "licensingServiceBinder"
        }
    .end annotation

    .line 396
    const-string p1, "LicenseClient"

    const-string v0, "Connected to the licensing service."

    invoke-static {p1, v0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 397
    sget-object p1, Lcom/pairip/licensecheck/LicenseClient;->licenseCheckState:Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;

    invoke-virtual {p1}, Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;->ordinal()I

    move-result p1

    if-eqz p1, :cond_2

    const/4 v0, 0x1

    if-eq p1, v0, :cond_1

    const/4 v0, 0x2

    if-eq p1, v0, :cond_0

    const/4 v0, 0x3

    if-eq p1, v0, :cond_1

    const/4 v0, 0x4

    if-eq p1, v0, :cond_2

    goto :goto_0

    .line 419
    :cond_0
    sget-object p1, Lcom/pairip/licensecheck/LicenseClient;->backgroundRunner:Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;

    new-instance v0, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda10;

    invoke-direct {v0, p0, p2}, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda10;-><init>(Lcom/pairip/licensecheck/LicenseClient;Landroid/os/IBinder;)V

    invoke-interface {p1, v0}, Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;->run(Ljava/lang/Runnable;)V

    goto :goto_0

    .line 400
    :cond_1
    invoke-virtual {p0}, Lcom/pairip/licensecheck/LicenseClient;->unbindFromLicensingService()V

    goto :goto_0

    .line 405
    :cond_2
    sget-object p1, Lcom/pairip/licensecheck/LicenseClient;->backgroundRunner:Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;

    new-instance v0, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda9;

    invoke-direct {v0, p0, p2}, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda9;-><init>(Lcom/pairip/licensecheck/LicenseClient;Landroid/os/IBinder;)V

    invoke-interface {p1, v0}, Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;->run(Ljava/lang/Runnable;)V

    :goto_0
    return-void
.end method

.method public onServiceDisconnected(Landroid/content/ComponentName;)V
    .locals 3
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x0
        }
        names = {
            "componentName"
        }
    .end annotation

    const/4 p1, 0x0

    .line 434
    iput-boolean p1, p0, Lcom/pairip/licensecheck/LicenseClient;->isBound:Z

    .line 435
    sget-object v0, Lcom/pairip/licensecheck/LicenseClient;->licenseCheckState:Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;

    sget-object v1, Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;->REPEATED_CHECK_REQUIRED:Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;

    invoke-virtual {v0, v1}, Lcom/pairip/licensecheck/LicenseClient$LicenseCheckState;->equals(Ljava/lang/Object;)Z

    move-result v0

    const-string v1, "LicenseClient"

    if-eqz v0, :cond_0

    iget-boolean v0, p0, Lcom/pairip/licensecheck/LicenseClient;->waitingForRepeatedCheck:Z

    if-eqz v0, :cond_0

    .line 437
    const-string p1, "Ignoring service disconnection in REPEATED_CHECK_REQUIRED state."

    invoke-static {v1, p1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_0

    .line 440
    :cond_0
    const-string v0, "Unexpectedly disconnected from the licensing service."

    invoke-static {v1, v0}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    .line 441
    invoke-static {}, Lcom/pairip/licensecheck/LicenseClient;->isLocalCheckPassed()Z

    move-result v0

    if-eqz v0, :cond_1

    .line 442
    sget-boolean v1, Lcom/pairip/licensecheck/LicenseClient;->backgroundLicensingServiceEnabled:Z

    if-eqz v1, :cond_1

    const/4 p1, 0x1

    .line 444
    :cond_1
    new-instance v1, Lcom/pairip/licensecheck/LicenseCheckException;

    const-string v2, "Licensing service unexpectedly disconnected."

    invoke-direct {v1, v2}, Lcom/pairip/licensecheck/LicenseCheckException;-><init>(Ljava/lang/String;)V

    invoke-direct {p0, v1, v0, p1}, Lcom/pairip/licensecheck/LicenseClient;->retryOrThrow(Lcom/pairip/licensecheck/LicenseCheckException;ZZ)V

    :goto_0
    return-void
.end method

.method public reportSuccessfulLicenseCheck(Landroid/os/IBinder;)V
    .locals 8
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x0
        }
        names = {
            "licensingServiceBinder"
        }
    .end annotation

    .annotation system Ldalvik/annotation/Throws;
        value = {
            Lcom/pairip/licensecheck/LicenseCheckException;
        }
    .end annotation

    .line 493
    const-string v0, "Request to licensing reporting service sent."

    .line 0
    const-string v1, "Error when calling licensing service."

    const/4 v2, 0x1

    if-nez p1, :cond_0

    .line 494
    new-instance p1, Lcom/pairip/licensecheck/LicenseCheckException;

    const-string v0, "Received a null binder."

    invoke-direct {p1, v0}, Lcom/pairip/licensecheck/LicenseCheckException;-><init>(Ljava/lang/String;)V

    sget-boolean v0, Lcom/pairip/licensecheck/LicenseClient;->backgroundLicensingServiceEnabled:Z

    invoke-direct {p0, p1, v2, v0}, Lcom/pairip/licensecheck/LicenseClient;->retryOrThrow(Lcom/pairip/licensecheck/LicenseCheckException;ZZ)V

    goto :goto_2

    .line 501
    :cond_0
    const-string v3, "Sending request to license reporting service..."

    const-string v4, "LicenseClient"

    invoke-static {v4, v3}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 502
    invoke-static {}, Landroid/os/Parcel;->obtain()Landroid/os/Parcel;

    move-result-object v3

    .line 503
    invoke-static {}, Landroid/os/Parcel;->obtain()Landroid/os/Parcel;

    move-result-object v5

    .line 505
    :try_start_0
    invoke-direct {p0, v3, p1}, Lcom/pairip/licensecheck/LicenseClient;->populateInputDataForReportAutoVerifiedLicense(Landroid/os/Parcel;Landroid/os/IBinder;)V

    const/4 v6, 0x3

    const/4 v7, 0x0

    .line 507
    invoke-interface {p1, v6, v3, v5, v7}, Landroid/os/IBinder;->transact(ILandroid/os/Parcel;Landroid/os/Parcel;I)Z

    move-result p1

    if-nez p1, :cond_1

    .line 510
    const-string p1, "Error sending request to license reporting service."

    invoke-static {v4, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_0

    .line 513
    :cond_1
    sget-object p1, Lcom/pairip/licensecheck/LicenseClient;->mainThreadRunner:Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;

    new-instance v6, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda12;

    invoke-direct {v6}, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda12;-><init>()V

    invoke-interface {p1, v6}, Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;->run(Ljava/lang/Runnable;)V
    :try_end_0
    .catch Landroid/os/DeadObjectException; {:try_start_0 .. :try_end_0} :catch_1
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 526
    :goto_0
    invoke-virtual {v3}, Landroid/os/Parcel;->recycle()V

    .line 527
    invoke-virtual {v5}, Landroid/os/Parcel;->recycle()V

    .line 528
    sget-object p1, Lcom/pairip/licensecheck/LicenseClient;->mainThreadRunner:Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;

    new-instance v1, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda0;

    invoke-direct {v1, p0}, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda0;-><init>(Lcom/pairip/licensecheck/LicenseClient;)V

    goto :goto_1

    :catch_0
    move-exception p1

    .line 524
    :try_start_1
    invoke-static {p1}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object p1

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2, v1}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v2, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-static {v4, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 526
    invoke-virtual {v3}, Landroid/os/Parcel;->recycle()V

    .line 527
    invoke-virtual {v5}, Landroid/os/Parcel;->recycle()V

    .line 528
    sget-object p1, Lcom/pairip/licensecheck/LicenseClient;->mainThreadRunner:Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;

    new-instance v1, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda0;

    invoke-direct {v1, p0}, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda0;-><init>(Lcom/pairip/licensecheck/LicenseClient;)V

    goto :goto_1

    :catch_1
    move-exception p1

    .line 519
    :try_start_2
    new-instance v1, Lcom/pairip/licensecheck/LicenseCheckException;

    const-string v6, "Licensing service process died."

    invoke-direct {v1, v6, p1}, Lcom/pairip/licensecheck/LicenseCheckException;-><init>(Ljava/lang/String;Ljava/lang/Throwable;)V

    sget-boolean p1, Lcom/pairip/licensecheck/LicenseClient;->backgroundLicensingServiceEnabled:Z

    invoke-direct {p0, v1, v2, p1}, Lcom/pairip/licensecheck/LicenseClient;->retryOrThrow(Lcom/pairip/licensecheck/LicenseCheckException;ZZ)V
    :try_end_2
    .catchall {:try_start_2 .. :try_end_2} :catchall_0

    .line 526
    invoke-virtual {v3}, Landroid/os/Parcel;->recycle()V

    .line 527
    invoke-virtual {v5}, Landroid/os/Parcel;->recycle()V

    .line 528
    sget-object p1, Lcom/pairip/licensecheck/LicenseClient;->mainThreadRunner:Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;

    new-instance v1, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda0;

    invoke-direct {v1, p0}, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda0;-><init>(Lcom/pairip/licensecheck/LicenseClient;)V

    :goto_1
    invoke-interface {p1, v1}, Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;->run(Ljava/lang/Runnable;)V

    .line 529
    invoke-static {v4, v0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    :goto_2
    return-void

    :catchall_0
    move-exception p1

    .line 526
    invoke-virtual {v3}, Landroid/os/Parcel;->recycle()V

    .line 527
    invoke-virtual {v5}, Landroid/os/Parcel;->recycle()V

    .line 528
    sget-object v1, Lcom/pairip/licensecheck/LicenseClient;->mainThreadRunner:Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;

    new-instance v2, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda0;

    invoke-direct {v2, p0}, Lcom/pairip/licensecheck/LicenseClient$$ExternalSyntheticLambda0;-><init>(Lcom/pairip/licensecheck/LicenseClient;)V

    invoke-interface {v1, v2}, Lcom/pairip/licensecheck/LicenseClient$ImmediateTaskExecutor;->run(Ljava/lang/Runnable;)V

    .line 529
    invoke-static {v4, v0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 530
    throw p1
.end method

.method protected unbindFromLicensingService()V
    .locals 3

    .line 688
    iget-boolean v0, p0, Lcom/pairip/licensecheck/LicenseClient;->isBound:Z

    if-eqz v0, :cond_0

    const/4 v0, 0x0

    .line 691
    iput-boolean v0, p0, Lcom/pairip/licensecheck/LicenseClient;->isBound:Z

    .line 693
    :try_start_0
    iget-object v0, p0, Lcom/pairip/licensecheck/LicenseClient;->context:Landroid/content/Context;

    invoke-virtual {v0, p0}, Landroid/content/Context;->unbindService(Landroid/content/ServiceConnection;)V
    :try_end_0
    .catch Ljava/lang/RuntimeException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    :catch_0
    move-exception v0

    .line 695
    const-string v1, "LicenseClient"

    const-string v2, "Failed to unbind from licensing service."

    invoke-static {v1, v2, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    :cond_0
    :goto_0
    return-void
.end method
