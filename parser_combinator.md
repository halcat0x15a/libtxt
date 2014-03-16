!SLIDE

# Markdownをparseする

[@halcat0x15a](http://twitter.com/halcat0x15a)

!SLIDE

# Parser combinator

[parser.clj](https://github.com/halcat0x15a/libtxt/blob/slide/src/libtxt/core/parser.clj)

* 生成
	* libtxt.core.parser/string
	* libtxt.core.parser/regex
* 変換
	* libtxt.core.parser/map
* 合成
	* libtxt.core.parser/many
	* libtxt.core.parser/choice
	* libtxt.core.parser/chain

!SLIDE

# Markdown

[markdown.clj](https://github.com/halcat0x15a/libtxt/blob/slide/src/libtxt/core/markdown.clj)

HTMLで出力する

[html.clj](https://github.com/halcat0x15a/libtxt/blob/slide/src/libtxt/core/html.clj)

!SLIDE

# 宣伝

* 3/19 Sendagaya.js
	* ClojureScriptのはなし
* 3/20 Shibuya.lisp
	* 型推論器とcore.logicのはなし