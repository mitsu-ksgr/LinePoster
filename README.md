LinePoster
==========

手軽にテキストや画像をラインに投稿出来るクラスです。

【 LinePoster 】
自分のアプリから、手軽にテキストや画像をラインに投稿したい！
という事があったので、パパっと投稿できるクラスを作ってみました。

テキスト、画像を投稿出来ます。
画像に関しては、アセット内にある画像も投稿出来るようにしました。
/data/data/app/files/にコピーしてから送信するので、なんかそこに保存する予定のある方は気をつけてください。
アセットから投稿した画像がギャラリーに表示されないので、便利かなーと思います。
(Android2.3 Digno ISW11Kでは表示されなかったよ！)

ラインがインストールされていない場合は、デフォルトではエラーコードを返すよになっていますが、ウェブに飛ばすようにも変更出来ます。
setSupportNotInstalledDevice()にtrueを渡してください。


トーストっぽく簡単に使えるように頑張ってみました。
テキスト投稿は、こんなに簡単にイケます。

LinePoster.make( context ).postMessage( "お前は今まで送信したメッセの数を覚えているのか？" );


/*
ソースのコメントは頑張って英語にしてみたけど面倒だし疲れたよママン。
ということで、ここは日本語のくせに、コメントだけ英語になってます。
しかも適当です。英語した動機も思い出せません。
*/