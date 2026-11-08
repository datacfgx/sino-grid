.class public final synthetic LGc/l0;
.super Ljava/lang/Object;
.source "R8$$SyntheticClass"

# interfaces
.implements Lkotlin/jvm/functions/Function1;


# instance fields
.field public final synthetic a:I

.field public final synthetic b:Ljava/lang/Object;

.field public final synthetic c:Ljava/lang/Object;


# direct methods
.method public synthetic constructor <init>(Ljava/lang/Object;ILjava/lang/Object;)V
    .locals 0

    .line 1
    iput p2, p0, LGc/l0;->a:I

    .line 2
    .line 3
    iput-object p1, p0, LGc/l0;->b:Ljava/lang/Object;

    .line 4
    .line 5
    iput-object p3, p0, LGc/l0;->c:Ljava/lang/Object;

    .line 6
    .line 7
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 8
    .line 9
    .line 10
    return-void
.end method


# virtual methods
.method public final invoke(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 10

    .line 1
    iget-object v0, p0, LGc/l0;->c:Ljava/lang/Object;

    .line 2
    .line 3
    iget-object v1, p0, LGc/l0;->b:Ljava/lang/Object;

    .line 4
    .line 5
    iget v2, p0, LGc/l0;->a:I

    .line 6
    .line 7
    packed-switch v2, :pswitch_data_0

    .line 8
    .line 9
    .line 10
    check-cast p1, Ljava/util/List;

    .line 11
    .line 12
    check-cast v1, Lorg/tezza/data/flow/FlowRepositoryImpl;

    .line 13
    .line 14
    check-cast v0, Le9/h;

    .line 15
    .line 16
    invoke-static {v1, v0, p1}, Lorg/tezza/data/flow/FlowRepositoryImpl;->e(Lorg/tezza/data/flow/FlowRepositoryImpl;Le9/h;Ljava/util/List;)Lkotlin/Unit;

    .line 17
    .line 18
    .line 19
    move-result-object p1

    .line 20
    return-object p1

    .line 21
    :pswitch_0
    check-cast p1, Ljava/lang/String;

    .line 22
    .line 23
    sget-object v2, LUc/y;->A0:Ljava/lang/String;

    .line 24
    .line 25
    const-string v2, "username"

    .line 26
    .line 27
    invoke-static {p1, v2}, Lkotlin/jvm/internal/Intrinsics;->checkNotNullParameter(Ljava/lang/Object;Ljava/lang/String;)V

    .line 28
    .line 29
    .line 30
    check-cast v1, LUc/a;

    .line 31
    .line 32
    const-string v2, "<set-?>"

    .line 33
    .line 34
    invoke-static {p1, v2}, Lkotlin/jvm/internal/Intrinsics;->checkNotNullParameter(Ljava/lang/Object;Ljava/lang/String;)V

    .line 35
    .line 36
    .line 37
    iput-object p1, v1, LUc/a;->b:Ljava/lang/String;

    .line 38
    .line 39
    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J

    .line 40
    .line 41
    .line 42
    move-result-wide v2

    .line 43
    iput-wide v2, v1, LUc/a;->f:J

    .line 44
    .line 45
    check-cast v0, LUc/y;

    .line 46
    .line 47
    invoke-virtual {v0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    .line 48
    .line 49
    .line 50
    new-instance p1, Lg9/a;

    .line 51
    .line 52
    invoke-direct {p1}, Ljava/lang/Object;-><init>()V

    .line 53
    .line 54
    .line 55
    new-instance v2, Lorg/tezza/domain/instafeed/AddInstagramAccount;

    .line 56
    .line 57
    invoke-direct {v2}, Lorg/tezza/domain/instafeed/AddInstagramAccount;-><init>()V

    .line 58
    .line 59
    .line 60
    new-instance v3, LUc/x;

    .line 61
    .line 62
    invoke-direct {v3, v1, v0}, LUc/x;-><init>(LUc/a;LUc/y;)V

    .line 63
    .line 64
    .line 65
    invoke-virtual {v2, v1, p1, v3}, Lzc/b;->execute(Ljava/lang/Object;Lg9/a;Lt9/a;)V

    .line 66
    .line 67
    .line 68
    sget-object p1, Lkotlin/Unit;->a:Lkotlin/Unit;

    .line 69
    .line 70
    return-object p1

    .line 71
    :pswitch_1
    check-cast p1, Ljava/util/ArrayList;

    .line 72
    .line 73
    sget-object v2, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;

    .line 74
    .line 75
    invoke-virtual {p1, v2}, Ljava/util/ArrayList;->contains(Ljava/lang/Object;)Z

    .line 76
    .line 77
    .line 78
    move-result v2

    .line 79
    check-cast v1, Ljava/util/List;

    .line 80
    .line 81
    const-string v3, "sendEvent() called with: eventName = Upload Media, properties = "

    .line 82
    .line 83
    const-string v4, "Uploaded Media Type"

    .line 84
    .line 85
    const-string v5, "Uploaded media count"

    .line 86
    .line 87
    const-string v6, "it"

    .line 88
    .line 89
    const-string v7, "CleverTap"

    .line 90
    .line 91
    const-string v8, "Upload Media"

    .line 92
    .line 93
    const-string v9, "isPhoto"

    .line 94
    .line 95
    if-eqz v2, :cond_0

    .line 96
    .line 97
    sget-object v2, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;

    .line 98
    .line 99
    invoke-virtual {p1, v2}, Ljava/util/ArrayList;->contains(Ljava/lang/Object;)Z

    .line 100
    .line 101
    .line 102
    move-result v2

    .line 103
    if-eqz v2, :cond_0

    .line 104
    .line 105
    invoke-interface {v1}, Ljava/util/List;->size()I

    .line 106
    .line 107
    .line 108
    move-result p1

    .line 109
    const-string v1, "Photo and Video"

    .line 110
    .line 111
    invoke-static {v1, v9}, Lkotlin/jvm/internal/Intrinsics;->checkNotNullParameter(Ljava/lang/Object;Ljava/lang/String;)V

    .line 112
    .line 113
    .line 114
    new-instance v2, Ljava/util/HashMap;

    .line 115
    .line 116
    invoke-direct {v2}, Ljava/util/HashMap;-><init>()V

    .line 117
    .line 118
    .line 119
    invoke-static {v2, v6}, Lkotlin/jvm/internal/Intrinsics;->checkNotNullParameter(Ljava/lang/Object;Ljava/lang/String;)V

    .line 120
    .line 121
    .line 122
    invoke-static {p1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    .line 123
    .line 124
    .line 125
    move-result-object p1

    .line 126
    invoke-virtual {v2, v5, p1}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 127
    .line 128
    .line 129
    invoke-virtual {v2, v4, v1}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 130
    .line 131
    .line 132
    sget-object p1, Lkotlin/Unit;->a:Lkotlin/Unit;

    .line 133
    .line 134
    new-instance p1, Ljava/lang/StringBuilder;

    .line 135
    .line 136
    invoke-direct {p1, v3}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    .line 137
    .line 138
    .line 139
    invoke-virtual {p1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    .line 140
    .line 141
    .line 142
    invoke-virtual {p1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    .line 143
    .line 144
    .line 145
    move-result-object p1

    .line 146
    invoke-static {v7, p1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 147
    .line 148
    .line 149
    sget-object p1, Lorg/tezza/presentation/ui/appmain/App;->a:Landroid/content/Context;

    .line 150
    .line 151
    invoke-static {}, Lorg/tezza/presentation/ui/appmain/App$b;->a()Landroid/content/Context;

    .line 152
    .line 153
    .line 154
    move-result-object p1

    .line 155
    invoke-static {p1}, Lcom/clevertap/android/sdk/a;->g(Landroid/content/Context;)Lcom/clevertap/android/sdk/a;

    .line 156
    .line 157
    .line 158
    move-result-object p1

    .line 159
    if-eqz p1, :cond_2

    .line 160
    .line 161
    invoke-virtual {p1, v8, v2}, Lcom/clevertap/android/sdk/a;->n(Ljava/lang/String;Ljava/util/HashMap;)V

    .line 162
    .line 163
    .line 164
    goto/16 :goto_0

    .line 165
    .line 166
    :cond_0
    sget-object v2, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;

    .line 167
    .line 168
    invoke-virtual {p1, v2}, Ljava/util/ArrayList;->contains(Ljava/lang/Object;)Z

    .line 169
    .line 170
    .line 171
    move-result p1

    .line 172
    if-eqz p1, :cond_1

    .line 173
    .line 174
    invoke-interface {v1}, Ljava/util/List;->size()I

    .line 175
    .line 176
    .line 177
    move-result p1

    .line 178
    const-string v1, "Photo"

    .line 179
    .line 180
    invoke-static {v1, v9}, Lkotlin/jvm/internal/Intrinsics;->checkNotNullParameter(Ljava/lang/Object;Ljava/lang/String;)V

    .line 181
    .line 182
    .line 183
    new-instance v2, Ljava/util/HashMap;

    .line 184
    .line 185
    invoke-direct {v2}, Ljava/util/HashMap;-><init>()V

    .line 186
    .line 187
    .line 188
    invoke-static {v2, v6}, Lkotlin/jvm/internal/Intrinsics;->checkNotNullParameter(Ljava/lang/Object;Ljava/lang/String;)V

    .line 189
    .line 190
    .line 191
    invoke-static {p1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    .line 192
    .line 193
    .line 194
    move-result-object p1

    .line 195
    invoke-virtual {v2, v5, p1}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 196
    .line 197
    .line 198
    invoke-virtual {v2, v4, v1}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 199
    .line 200
    .line 201
    sget-object p1, Lkotlin/Unit;->a:Lkotlin/Unit;

    .line 202
    .line 203
    new-instance p1, Ljava/lang/StringBuilder;

    .line 204
    .line 205
    invoke-direct {p1, v3}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    .line 206
    .line 207
    .line 208
    invoke-virtual {p1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    .line 209
    .line 210
    .line 211
    invoke-virtual {p1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    .line 212
    .line 213
    .line 214
    move-result-object p1

    .line 215
    invoke-static {v7, p1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 216
    .line 217
    .line 218
    sget-object p1, Lorg/tezza/presentation/ui/appmain/App;->a:Landroid/content/Context;

    .line 219
    .line 220
    invoke-static {}, Lorg/tezza/presentation/ui/appmain/App$b;->a()Landroid/content/Context;

    .line 221
    .line 222
    .line 223
    move-result-object p1

    .line 224
    invoke-static {p1}, Lcom/clevertap/android/sdk/a;->g(Landroid/content/Context;)Lcom/clevertap/android/sdk/a;

    .line 225
    .line 226
    .line 227
    move-result-object p1

    .line 228
    if-eqz p1, :cond_2

    .line 229
    .line 230
    invoke-virtual {p1, v8, v2}, Lcom/clevertap/android/sdk/a;->n(Ljava/lang/String;Ljava/util/HashMap;)V

    .line 231
    .line 232
    .line 233
    goto :goto_0

    .line 234
    :cond_1
    invoke-interface {v1}, Ljava/util/List;->size()I

    .line 235
    .line 236
    .line 237
    move-result p1

    .line 238
    const-string v1, "Video"

    .line 239
    .line 240
    invoke-static {v1, v9}, Lkotlin/jvm/internal/Intrinsics;->checkNotNullParameter(Ljava/lang/Object;Ljava/lang/String;)V

    .line 241
    .line 242
    .line 243
    new-instance v2, Ljava/util/HashMap;

    .line 244
    .line 245
    invoke-direct {v2}, Ljava/util/HashMap;-><init>()V

    .line 246
    .line 247
    .line 248
    invoke-static {v2, v6}, Lkotlin/jvm/internal/Intrinsics;->checkNotNullParameter(Ljava/lang/Object;Ljava/lang/String;)V

    .line 249
    .line 250
    .line 251
    invoke-static {p1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    .line 252
    .line 253
    .line 254
    move-result-object p1

    .line 255
    invoke-virtual {v2, v5, p1}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 256
    .line 257
    .line 258
    invoke-virtual {v2, v4, v1}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 259
    .line 260
    .line 261
    sget-object p1, Lkotlin/Unit;->a:Lkotlin/Unit;

    .line 262
    .line 263
    new-instance p1, Ljava/lang/StringBuilder;

    .line 264
    .line 265
    invoke-direct {p1, v3}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    .line 266
    .line 267
    .line 268
    invoke-virtual {p1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    .line 269
    .line 270
    .line 271
    invoke-virtual {p1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    .line 272
    .line 273
    .line 274
    move-result-object p1

    .line 275
    invoke-static {v7, p1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 276
    .line 277
    .line 278
    sget-object p1, Lorg/tezza/presentation/ui/appmain/App;->a:Landroid/content/Context;

    .line 279
    .line 280
    invoke-static {}, Lorg/tezza/presentation/ui/appmain/App$b;->a()Landroid/content/Context;

    .line 281
    .line 282
    .line 283
    move-result-object p1

    .line 284
    invoke-static {p1}, Lcom/clevertap/android/sdk/a;->g(Landroid/content/Context;)Lcom/clevertap/android/sdk/a;

    .line 285
    .line 286
    .line 287
    move-result-object p1

    .line 288
    if-eqz p1, :cond_2

    .line 289
    .line 290
    invoke-virtual {p1, v8, v2}, Lcom/clevertap/android/sdk/a;->n(Ljava/lang/String;Ljava/util/HashMap;)V

    .line 291
    .line 292
    .line 293
    :cond_2
    :goto_0
    check-cast v0, LLc/X;

    .line 294
    .line 295
    invoke-virtual {v0}, LLc/X;->t()V

    .line 296
    .line 297
    .line 298
    sget-object p1, Lkotlin/Unit;->a:Lkotlin/Unit;

    .line 299
    .line 300
    return-object p1

    .line 301
    :pswitch_2
    check-cast p1, Ljava/lang/Boolean;

    # +++ ENI: veredito da assinatura forçado pra true (libera todos os filtros) +++
    const/4 v8, 0x1
    invoke-static {v8}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
    move-result-object p1

    .line 302
    .line 303
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z

    .line 304
    .line 305
    .line 306
    check-cast v1, Lkotlin/jvm/internal/O;

    .line 307
    .line 308
    iget-boolean v2, v1, Lkotlin/jvm/internal/O;->a:Z

    .line 309
    .line 310
    if-nez v2, :cond_3

    .line 311
    .line 312
    const/4 v2, 0x1

    .line 313
    iput-boolean v2, v1, Lkotlin/jvm/internal/O;->a:Z

    .line 314
    .line 315
    check-cast v0, Lkotlin/jvm/functions/Function1;

    .line 316
    .line 317
    invoke-interface {v0, p1}, Lkotlin/jvm/functions/Function1;->invoke(Ljava/lang/Object;)Ljava/lang/Object;

    .line 318
    .line 319
    .line 320
    :cond_3
    sget-object p1, Lkotlin/Unit;->a:Lkotlin/Unit;

    .line 321
    .line 322
    return-object p1

    .line 323
    :pswitch_data_0
    .packed-switch 0x0
        :pswitch_2
        :pswitch_1
        :pswitch_0
    .end packed-switch
.end method
