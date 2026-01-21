# Applied-Replicatics

[日本語](README_JP.md) | [English](README.md)

このMod は Applied Energistics 2 と Replication を統合するアドオンです。

## 機能

### AE2 へのマターの統合

Replication のマターを Applied Energistics 2 で扱うための機能を提供します。

これは、既存のMod による擬似的な統合ではなく、AE2 の標準的な機能を利用した本格的な統合です。

### マターストレージセル

マターを保存するためのストレージセルを追加します。

MEGA Cells との互換性があり、1k から 256M までの容量があります。

基本的な仕様は他のストレージセルと同様ですが、256 マターで 1 バイト消費します。

> [!NOTE]
> 利用には、複製ネットワークとマターをやり取りするための ME Replication Connector が必要です。
> 
> 似たような働きをする別の Mod のブロックとは互換性が無いことに注意してください。

### ME Replication Connector

複製ネットワークと MEネットワークを相互に接続するためのブロックです。

マターを、複製ネットワークから MEネットワークに保存したり、複製ネットワークから取り出したりすることができます。

また、チップ保管機に保存されたパターンは MEネットワークへ「自動クラフト可能」として登録されます。

自動クラフト時の複製機の結果は ME Replication Connector 経由で MEネットワークへ搬入されます。

---

## ライセンス

このMod のソースコードは [GNU Lesser General Public License v3.0 (LGPL-3.0)](https://www.gnu.org/licenses/lgpl-3.0.html) の下でライセンスされています。

ソースコードは [GitHub リポジトリ](https://github.com/Lapis256/Applied-Replicatics) より入手可能です。

アセットは [Creative Commons Attribution-NonCommercial-ShareAlike 3.0 (CC BY-NC-SA 3.0)](https://creativecommons.org/licenses/by-nc-sa/3.0/)
の下でライセンスされています。

### 依存関係とそのライセンス

このMod は以下のプロジェクトに依存しています:

- [Applied-Energistics-2](https://github.com/AppliedEnergistics/Applied-Energistics-2): [LGPL-3.0](https://www.gnu.org/licenses/lgpl-3.0.html)
  の下でライセンスされています
- [Replication](https://github.com/Buuz135/Replication): [MIT](https://github.com/Buuz135/Replication/blob/1.21/LICENSE) の下でライセンスされています
- [MEGACells](https://github.com/62832/MEGACells): [LGPL-3.0](https://www.gnu.org/licenses/lgpl-3.0.html) の下でライセンスされています

### アセットとそのライセンス

このMod のテクスチャは以下のプロジェクトに由来しています:

- [Applied-Energistics-2](https://github.com/AppliedEnergistics/Applied-Energistics-2): [CC BY-NC-SA 3.0](https://creativecommons.org/licenses/by-nc-sa/3.0/)
  の下でライセンスされています
- [Replication](https://github.com/Buuz135/Replication): [MIT](https://github.com/Buuz135/Replication/blob/1.21/LICENSE) の下でライセンスされています
- [MEGACells](https://github.com/62832/MEGACells): [CC BY-NC-SA 3.0](https://creativecommons.org/licenses/by-nc-sa/3.0/) の下でライセンスされています

従って前述の通り、このMod のアセットは [CC BY-NC-SA 3.0](https://creativecommons.org/licenses/by-nc-sa/3.0/) の下でライセンスされており、その使用はこのライセンスの条件に従う必要があります。
