.class public final Lcom/example/flipperdroid/model/object/SwiftPairAdvertisementSetGenerator;
.super Ljava/lang/Object;
.source "SwiftPairAdvertisementSetGenerator.kt"


# static fields
.field public static final INSTANCE:Lcom/example/flipperdroid/model/object/SwiftPairAdvertisementSetGenerator;

.field private static final deviceNames:[Ljava/lang/String;


# direct methods
.method static constructor <clinit>()V
    .locals 4

    new-instance v0, Lcom/example/flipperdroid/model/object/SwiftPairAdvertisementSetGenerator;

    invoke-direct {v0}, Lcom/example/flipperdroid/model/object/SwiftPairAdvertisementSetGenerator;-><init>()V

    sput-object v0, Lcom/example/flipperdroid/model/object/SwiftPairAdvertisementSetGenerator;->INSTANCE:Lcom/example/flipperdroid/model/object/SwiftPairAdvertisementSetGenerator;

    const/4 v0, 0x5

    new-array v0, v0, [Ljava/lang/String;

    const/4 v1, 0x0

    const-string v2, "Mouse"

    aput-object v2, v0, v1

    const/4 v1, 0x1

    const-string v2, "Keyboard"

    aput-object v2, v0, v1

    const/4 v1, 0x2

    const-string v2, "AirPods"

    aput-object v2, v0, v1

    const/4 v1, 0x3

    const-string v2, "Flipper Z"

    aput-object v2, v0, v1

    const/4 v1, 0x4

    const-string v2, "Free Wi-Fi"

    aput-object v2, v0, v1

    sput-object v0, Lcom/example/flipperdroid/model/object/SwiftPairAdvertisementSetGenerator;->deviceNames:[Ljava/lang/String;

    return-void
.end method

.method private constructor <init>()V
    .locals 0

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public final getAdvertisementSets()Ljava/util/List;
    .locals 12

    new-instance v0, Ljava/util/ArrayList;

    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    check-cast v0, Ljava/util/List;

    sget-object v1, Lcom/example/flipperdroid/model/object/SwiftPairAdvertisementSetGenerator;->deviceNames:[Ljava/lang/String;

    array-length v2, v1

    const/4 v3, 0x0

    :goto_name
    if-ge v3, v2, :done_all

    new-instance v6, Lcom/example/flipperdroid/model/class/AdvertisementSet;

    invoke-direct {v6}, Lcom/example/flipperdroid/model/class/AdvertisementSet;-><init>()V

    sget-object v7, Lcom/example/flipperdroid/model/enums/AdvertisementTarget;->ADVERTISEMENT_TARGET_WINDOWS:Lcom/example/flipperdroid/model/enums/AdvertisementTarget;

    invoke-virtual {v6, v7}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->setTarget(Lcom/example/flipperdroid/model/enums/AdvertisementTarget;)V

    sget-object v7, Lcom/example/flipperdroid/model/enums/AdvertisementSetType;->ADVERTISEMENT_TYPE_UNDEFINED:Lcom/example/flipperdroid/model/enums/AdvertisementSetType;

    invoke-virtual {v6, v7}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->setType(Lcom/example/flipperdroid/model/enums/AdvertisementSetType;)V

    sget-object v7, Lcom/example/flipperdroid/model/enums/AdvertisementSetRange;->ADVERTISEMENTSET_RANGE_CLOSE:Lcom/example/flipperdroid/model/enums/AdvertisementSetRange;

    invoke-virtual {v6, v7}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->setRange(Lcom/example/flipperdroid/model/enums/AdvertisementSetRange;)V

    aget-object v7, v1, v3

    invoke-virtual {v6, v7}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->setTitle(Ljava/lang/String;)V

    invoke-virtual {v6}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->getAdvertiseSettings()Lcom/example/flipperdroid/model/class/AdvertiseSettings;

    move-result-object v7

    sget-object v8, Lcom/example/flipperdroid/model/enums/AdvertiseMode;->ADVERTISEMODE_LOW_LATENCY:Lcom/example/flipperdroid/model/enums/AdvertiseMode;

    invoke-virtual {v7, v8}, Lcom/example/flipperdroid/model/class/AdvertiseSettings;->setAdvertiseMode(Lcom/example/flipperdroid/model/enums/AdvertiseMode;)V

    invoke-virtual {v6}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->getAdvertiseSettings()Lcom/example/flipperdroid/model/class/AdvertiseSettings;

    move-result-object v7

    sget-object v8, Lcom/example/flipperdroid/model/enums/TxPowerLevel;->TX_POWER_HIGH:Lcom/example/flipperdroid/model/enums/TxPowerLevel;

    invoke-virtual {v7, v8}, Lcom/example/flipperdroid/model/class/AdvertiseSettings;->setTxPowerLevel(Lcom/example/flipperdroid/model/enums/TxPowerLevel;)V

    invoke-virtual {v6}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->getAdvertiseSettings()Lcom/example/flipperdroid/model/class/AdvertiseSettings;

    move-result-object v7

    const/4 v8, 0x0

    invoke-virtual {v7, v8}, Lcom/example/flipperdroid/model/class/AdvertiseSettings;->setConnectable(Z)V

    invoke-virtual {v6}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->getAdvertiseSettings()Lcom/example/flipperdroid/model/class/AdvertiseSettings;

    move-result-object v7

    const/4 v8, 0x0

    invoke-virtual {v7, v8}, Lcom/example/flipperdroid/model/class/AdvertiseSettings;->setTimeout(I)V

    invoke-virtual {v6}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->getAdvertisingSetParameters()Lcom/example/flipperdroid/model/class/AdvertisingSetParameters;

    move-result-object v7

    const/4 v8, 0x1

    invoke-virtual {v7, v8}, Lcom/example/flipperdroid/model/class/AdvertisingSetParameters;->setLegacyMode(Z)V

    invoke-virtual {v6}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->getAdvertisingSetParameters()Lcom/example/flipperdroid/model/class/AdvertisingSetParameters;

    move-result-object v7

    sget-object v8, Lcom/example/flipperdroid/model/enums/TxPowerLevel;->TX_POWER_HIGH:Lcom/example/flipperdroid/model/enums/TxPowerLevel;

    invoke-virtual {v7, v8}, Lcom/example/flipperdroid/model/class/AdvertisingSetParameters;->setTxPowerLevel(Lcom/example/flipperdroid/model/enums/TxPowerLevel;)V

    invoke-virtual {v6}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->getAdvertisingSetParameters()Lcom/example/flipperdroid/model/class/AdvertisingSetParameters;

    move-result-object v7

    const/4 v8, 0x1

    invoke-virtual {v7, v8}, Lcom/example/flipperdroid/model/class/AdvertisingSetParameters;->setScanable(Z)V

    invoke-virtual {v6}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->getAdvertisingSetParameters()Lcom/example/flipperdroid/model/class/AdvertisingSetParameters;

    move-result-object v7

    const/4 v8, 0x0

    invoke-virtual {v7, v8}, Lcom/example/flipperdroid/model/class/AdvertisingSetParameters;->setConnectable(Z)V

    invoke-virtual {v6}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->getAdvertiseData()Lcom/example/flipperdroid/model/class/AdvertiseData;

    move-result-object v7

    const/4 v8, 0x1

    invoke-virtual {v7, v8}, Lcom/example/flipperdroid/model/class/AdvertiseData;->setIncludeDeviceName(Z)V

    new-instance v9, Lcom/example/flipperdroid/model/class/ManufacturerSpecificData;

    invoke-direct {v9}, Lcom/example/flipperdroid/model/class/ManufacturerSpecificData;-><init>()V

    const/16 v7, 0x6

    invoke-virtual {v9, v7}, Lcom/example/flipperdroid/model/class/ManufacturerSpecificData;->setManufacturerId(I)V

    const/4 v7, 0x5

    new-array v10, v7, [B

    const/4 v7, 0x3

    int-to-byte v7, v7

    const/4 v11, 0x0

    aput-byte v7, v10, v11

    const/4 v7, 0x0

    int-to-byte v7, v7

    const/4 v11, 0x1

    aput-byte v7, v10, v11

    const/4 v7, 0x1

    int-to-byte v7, v7

    const/4 v11, 0x2

    aput-byte v7, v10, v11

    const/4 v7, 0x0

    int-to-byte v7, v7

    const/4 v11, 0x3

    aput-byte v7, v10, v11

    const/4 v7, 0x0

    int-to-byte v7, v7

    const/4 v11, 0x4

    aput-byte v7, v10, v11

    invoke-virtual {v9, v10}, Lcom/example/flipperdroid/model/class/ManufacturerSpecificData;->setManufacturerSpecificData([B)V

    invoke-virtual {v6}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->getAdvertiseData()Lcom/example/flipperdroid/model/class/AdvertiseData;

    move-result-object v7

    invoke-virtual {v7}, Lcom/example/flipperdroid/model/class/AdvertiseData;->getManufacturerData()Ljava/util/List;

    move-result-object v7

    invoke-interface {v7, v9}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    invoke-interface {v0, v6}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    add-int/lit8 v3, v3, 0x1

    goto :goto_name

    :done_all
    return-object v0
.end method
