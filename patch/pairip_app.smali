.class public Lcom/pairip/application/Application;
.super Lorg/tezza/presentation/ui/appmain/App;


# direct methods
.method public constructor <init>()V
    .locals 0

    invoke-direct {p0}, Lorg/tezza/presentation/ui/appmain/App;-><init>()V

    return-void
.end method


# virtual methods
.method protected attachBaseContext(Landroid/content/Context;)V
    .locals 0

    invoke-static {p1}, Lcom/pairip/VMRunner;->setContext(Landroid/content/Context;)V

    # +++ ENI: verificacao de assinatura NOPada (Pairip) +++

    # +++ ENI: checkLicense NOPado - app roda sem a tela da Play Store +++

    invoke-super {p0, p1}, Lcom/pairip/application/Application;->attachBaseContext(Landroid/content/Context;)V

    return-void
.end method
