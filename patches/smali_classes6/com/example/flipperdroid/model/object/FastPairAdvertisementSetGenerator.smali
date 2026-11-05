.class public final Lcom/example/flipperdroid/model/object/FastPairAdvertisementSetGenerator;
.super Ljava/lang/Object;
.source "FastPairAdvertisementSetGenerator.kt"


# static fields
.field public static final INSTANCE:Lcom/example/flipperdroid/model/object/FastPairAdvertisementSetGenerator;

.field private static final modelIds:[I

.field private static final setsPerModel:I


# direct methods
.method static constructor <clinit>()V
    .locals 4

    new-instance v0, Lcom/example/flipperdroid/model/object/FastPairAdvertisementSetGenerator;

    invoke-direct {v0}, Lcom/example/flipperdroid/model/object/FastPairAdvertisementSetGenerator;-><init>()V

    sput-object v0, Lcom/example/flipperdroid/model/object/FastPairAdvertisementSetGenerator;->INSTANCE:Lcom/example/flipperdroid/model/object/FastPairAdvertisementSetGenerator;

    const/16 v0, 0xa

    new-array v0, v0, [I

    const/4 v1, 0x0

    const v2, 0x1c5

    aput v2, v0, v1

    const/4 v1, 0x1

    const v2, 0xe98

    aput v2, v0, v1

    const/4 v1, 0x2

    const v2, 0xf0c

    aput v2, v0, v1

    const/4 v1, 0x3

    const v2, 0x2000

    aput v2, v0, v1

    const/4 v1, 0x4

    const v2, 0x3001

    aput v2, v0, v1

    const/4 v1, 0x5

    const v2, 0x3b41

    aput v2, v0, v1

    const/4 v1, 0x6

    const v2, 0x5bc9

    aput v2, v0, v1

    const/4 v1, 0x7

    const v2, 0x8263

    aput v2, v0, v1

    const/16 v1, 0x8

    const v2, 0xa168

    aput v2, v0, v1

    const/16 v1, 0x9

    const v2, 0xc95c

    aput v2, v0, v1

    sput-object v0, Lcom/example/flipperdroid/model/object/FastPairAdvertisementSetGenerator;->modelIds:[I

    const/4 v0, 0x1

    sput v0, Lcom/example/flipperdroid/model/object/FastPairAdvertisementSetGenerator;->setsPerModel:I

    return-void
.end method

.method private constructor <init>()V
    .locals 0

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public final getAdvertisementSets()Ljava/util/List;
    .locals 14

    new-instance v0, Ljava/util/ArrayList;

    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    check-cast v0, Ljava/util/List;

    sget-object v1, Lcom/example/flipperdroid/model/object/FastPairAdvertisementSetGenerator;->modelIds:[I

    array-length v2, v1

    const/4 v3, 0x0

    :goto_model
    if-ge v3, v2, :done_all

    aget v4, v1, v3

    const/4 v5, 0x0

    :goto_salt
    sget v6, Lcom/example/flipperdroid/model/object/FastPairAdvertisementSetGenerator;->setsPerModel:I

    if-ge v5, v6, :next_model

    new-instance v7, Lcom/example/flipperdroid/model/class/AdvertisementSet;

    invoke-direct {v7}, Lcom/example/flipperdroid/model/class/AdvertisementSet;-><init>()V

    sget-object v8, Lcom/example/flipperdroid/model/enums/AdvertisementTarget;->ADVERTISEMENT_TARGET_ANDROID:Lcom/example/flipperdroid/model/enums/AdvertisementTarget;

    invoke-virtual {v7, v8}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->setTarget(Lcom/example/flipperdroid/model/enums/AdvertisementTarget;)V

    sget-object v8, Lcom/example/flipperdroid/model/enums/AdvertisementSetType;->ADVERTISEMENT_TYPE_UNDEFINED:Lcom/example/flipperdroid/model/enums/AdvertisementSetType;

    invoke-virtual {v7, v8}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->setType(Lcom/example/flipperdroid/model/enums/AdvertisementSetType;)V

    sget-object v8, Lcom/example/flipperdroid/model/enums/AdvertisementSetRange;->ADVERTISEMENTSET_RANGE_CLOSE:Lcom/example/flipperdroid/model/enums/AdvertisementSetRange;

    invoke-virtual {v7, v8}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->setRange(Lcom/example/flipperdroid/model/enums/AdvertisementSetRange;)V

    new-instance v8, Ljava/lang/StringBuilder;

    invoke-direct {v8}, Ljava/lang/StringBuilder;-><init>()V

    const-string v11, "Fast Pair "

    invoke-virtual {v8, v11}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    add-int/lit8 v11, v3, 0x1

    invoke-static {v11}, Ljava/lang/Integer;->toString(I)Ljava/lang/String;

    move-result-object v11

    invoke-virtual {v8, v11}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v8}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v8

    invoke-virtual {v7, v8}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->setTitle(Ljava/lang/String;)V

    invoke-virtual {v7}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->getAdvertiseSettings()Lcom/example/flipperdroid/model/class/AdvertiseSettings;

    move-result-object v8

    sget-object v9, Lcom/example/flipperdroid/model/enums/AdvertiseMode;->ADVERTISEMODE_LOW_LATENCY:Lcom/example/flipperdroid/model/enums/AdvertiseMode;

    invoke-virtual {v8, v9}, Lcom/example/flipperdroid/model/class/AdvertiseSettings;->setAdvertiseMode(Lcom/example/flipperdroid/model/enums/AdvertiseMode;)V

    invoke-virtual {v7}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->getAdvertiseSettings()Lcom/example/flipperdroid/model/class/AdvertiseSettings;

    move-result-object v8

    sget-object v9, Lcom/example/flipperdroid/model/enums/TxPowerLevel;->TX_POWER_HIGH:Lcom/example/flipperdroid/model/enums/TxPowerLevel;

    invoke-virtual {v8, v9}, Lcom/example/flipperdroid/model/class/AdvertiseSettings;->setTxPowerLevel(Lcom/example/flipperdroid/model/enums/TxPowerLevel;)V

    invoke-virtual {v7}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->getAdvertiseSettings()Lcom/example/flipperdroid/model/class/AdvertiseSettings;

    move-result-object v8

    const/4 v9, 0x0

    invoke-virtual {v8, v9}, Lcom/example/flipperdroid/model/class/AdvertiseSettings;->setConnectable(Z)V

    invoke-virtual {v7}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->getAdvertiseSettings()Lcom/example/flipperdroid/model/class/AdvertiseSettings;

    move-result-object v8

    const/4 v9, 0x0

    invoke-virtual {v8, v9}, Lcom/example/flipperdroid/model/class/AdvertiseSettings;->setTimeout(I)V

    invoke-virtual {v7}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->getAdvertisingSetParameters()Lcom/example/flipperdroid/model/class/AdvertisingSetParameters;

    move-result-object v8

    const/4 v9, 0x1

    invoke-virtual {v8, v9}, Lcom/example/flipperdroid/model/class/AdvertisingSetParameters;->setLegacyMode(Z)V

    invoke-virtual {v7}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->getAdvertisingSetParameters()Lcom/example/flipperdroid/model/class/AdvertisingSetParameters;

    move-result-object v8

    sget-object v9, Lcom/example/flipperdroid/model/enums/TxPowerLevel;->TX_POWER_HIGH:Lcom/example/flipperdroid/model/enums/TxPowerLevel;

    invoke-virtual {v8, v9}, Lcom/example/flipperdroid/model/class/AdvertisingSetParameters;->setTxPowerLevel(Lcom/example/flipperdroid/model/enums/TxPowerLevel;)V

    invoke-virtual {v7}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->getAdvertisingSetParameters()Lcom/example/flipperdroid/model/class/AdvertisingSetParameters;

    move-result-object v8

    const/4 v9, 0x1

    invoke-virtual {v8, v9}, Lcom/example/flipperdroid/model/class/AdvertisingSetParameters;->setScanable(Z)V

    invoke-virtual {v7}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->getAdvertisingSetParameters()Lcom/example/flipperdroid/model/class/AdvertisingSetParameters;

    move-result-object v8

    const/4 v9, 0x0

    invoke-virtual {v8, v9}, Lcom/example/flipperdroid/model/class/AdvertisingSetParameters;->setConnectable(Z)V

    invoke-virtual {v7}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->getAdvertiseData()Lcom/example/flipperdroid/model/class/AdvertiseData;

    move-result-object v8

    const/4 v9, 0x0

    invoke-virtual {v8, v9}, Lcom/example/flipperdroid/model/class/AdvertiseData;->setIncludeDeviceName(Z)V

    new-instance v9, Lcom/example/flipperdroid/model/class/ServiceData;

    invoke-direct {v9}, Lcom/example/flipperdroid/model/class/ServiceData;-><init>()V

    const-string v8, "0000fe2c-0000-1000-8000-00805f9b34fb"

    invoke-static {v8}, Ljava/util/UUID;->fromString(Ljava/lang/String;)Ljava/util/UUID;

    move-result-object v8

    invoke-virtual {v9, v8}, Lcom/example/flipperdroid/model/class/ServiceData;->setServiceUuid(Ljava/util/UUID;)V

    const/4 v8, 0x4

    new-array v10, v8, [B

    and-int/lit16 v11, v4, 0xff

    int-to-byte v11, v11

    const/4 v12, 0x0

    aput-byte v11, v10, v12

    shr-int/lit8 v11, v4, 0x8

    and-int/lit16 v11, v11, 0xff

    int-to-byte v11, v11

    const/4 v12, 0x1

    aput-byte v11, v10, v12

    shr-int/lit8 v11, v4, 0x10

    and-int/lit16 v11, v11, 0xff

    int-to-byte v11, v11

    const/4 v12, 0x2

    aput-byte v11, v10, v12

    sget-object v11, Lkotlin/random/Random;->Default:Lkotlin/random/Random$Default;

    const/16 v12, 0x100

    invoke-virtual {v11, v12}, Lkotlin/random/Random$Default;->nextInt(I)I

    move-result v11

    int-to-byte v11, v11

    const/4 v12, 0x3

    aput-byte v11, v10, v12

    invoke-virtual {v9, v10}, Lcom/example/flipperdroid/model/class/ServiceData;->setServiceData([B)V

    invoke-virtual {v7}, Lcom/example/flipperdroid/model/class/AdvertisementSet;->getAdvertiseData()Lcom/example/flipperdroid/model/class/AdvertiseData;

    move-result-object v8

    invoke-virtual {v8}, Lcom/example/flipperdroid/model/class/AdvertiseData;->getServices()Ljava/util/List;

    move-result-object v8

    invoke-interface {v8, v9}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    invoke-interface {v0, v7}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    add-int/lit8 v5, v5, 0x1

    goto :goto_salt

    :next_model
    add-int/lit8 v3, v3, 0x1

    goto :goto_model

    :done_all
    return-object v0
.end method
