package red.man10.man10itembank.menu

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryAction
import red.man10.man10itembank.ItemData
import red.man10.man10itembank.Log
import red.man10.man10itembank.Man10ItemBank
import red.man10.man10itembank.util.Utility

class TakeMenu(p:Player,private val page : Int) : MenuFramework(p,54,"アイテムを取り出す"){

    override fun init() {

        //アイテムを入れられないように
        setClickAction(){ it.isCancelled = true }

        val itemIndex = ItemData.getItemIndexMap().values.toList()

        var inc = 0

        while (menu.getItem(44)==null){

            val index = inc+page*45
            inc++
            if (itemIndex.size<=index) break

            val data = itemIndex[index]
            val button = Button(data.item!!.type)
            button.title("§b${data.itemKey}")
            if (data.item!!.hasItemMeta() && data.item!!.itemMeta.hasCustomModelData()){
                button.cmd(data.item!!.itemMeta.customModelData)
            }

            button.setClickAction{e,->

                if (e.action == InventoryAction.PICKUP_ALL){
                    takeItemFromItemStorage(p,data.id,1)
                }

                if (e.action == InventoryAction.MOVE_TO_OTHER_INVENTORY){
                    takeItemFromItemStorage(p,data.id,data.item!!.maxStackSize)
                }

                //評価額、個数を確認
                ItemData.getItemAmount(p.uniqueId,data.id){

                    button.enchant(it>0)

                    button.lore(mutableListOf(
                        "§a所有数:§e${Utility.format(it)}",
                        "§a単価:§e${Utility.format(data.bid)}",
                        "§a推定評価額:§e${Utility.format(data.bid*it.toDouble())}"))

                    Bukkit.getScheduler().runTask(Man10ItemBank.plugin, Runnable { setButton(button,e.slot) })
                }

            }

            //評価額、個数を確認
            ItemData.getItemAmount(p.uniqueId,data.id){

                button.enchant(it>0)

                button.lore(mutableListOf(
                    "§a所有数:§e${Utility.format(it)}",
                    "§a単価:§e${Utility.format(data.bid)}",
                    "§a推定評価額:§e${Utility.format(data.bid*it.toDouble())}"))

                Bukkit.getScheduler().runTask(Man10ItemBank.plugin, Runnable {
                    menu.addItem(button.icon())
                })
            }
        }

        //Back
        val back = Button(Material.LIGHT_BLUE_STAINED_GLASS_PANE)
        back.title("")
        arrayOf(45,46,47,48,49,50,51,52,53).forEach { setButton(back,it) }

        //previous
        if (page!=0){
            val previous = Button(Material.RED_STAINED_GLASS_PANE)
            previous.title("前のページへ")
            previous.setClickAction{ TakeMenu(p,page-1).open() }
            arrayOf(45,46,47).forEach { setButton(previous,it) }

        }

        //next
        if (inc>=44){
            val next = Button(Material.RED_STAINED_GLASS_PANE)
            next.title("次のページへ")
            next.setClickAction{ TakeMenu(p,page+1).open() }
            arrayOf(51,52,53).forEach { setButton(next,it) }
        }


    }

    private fun takeItemFromItemStorage(p:Player, id:Int, amount:Int){

        ItemData.takeItemAmount(p.uniqueId,p.uniqueId,id,amount){
            if (it==null){
                Utility.sendError(p,"在庫ないので取り出すことができません")
                return@takeItemAmount
            }

            Bukkit.getScheduler().runTask(Man10ItemBank.plugin, Runnable {
                if (p.inventory.firstEmpty()==-1){
                    Utility.sendError(p,"インベントリがいっぱいなので取り出すことができません")
                    Log.storageLog(p.uniqueId,p.uniqueId,id,0,0,"FailedTakingItem(FullOfInventory)")
                    ItemData.addItemAmount(p.uniqueId,p.uniqueId,id,amount)
                    return@Runnable
                }

                val data = ItemData.getItemData(id)!!
                Utility.sendMsg(p,"${amount}個取り出しました(現在の在庫:${Utility.format(it)})")

                val clone = data.item!!.clone()

                clone.amount = amount
                p.inventory.addItem(clone)
            })
        }


    }

}