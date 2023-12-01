package pw.mihou.nexus.features.react.elements

import org.javacord.api.entity.Icon
import org.javacord.api.entity.message.MessageAuthor
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.user.User
import pw.mihou.nexus.features.react.React
import pw.mihou.nexus.features.react.styles.TextStyles
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.time.Instant

fun React.Component.Embed(embed: Embed.() -> Unit) {
    val element = Embed()
    embed(element)

    embeds.add(element.embed)
}

class Embed: TextStyles {
    internal val embed = EmbedBuilder()

    fun Title(text: String) {
        embed.setTitle(text)
    }

    fun Body(vararg nodes: String) {
        embed.setDescription(nodes.joinToString(""))
    }
    fun SpacedBody(vararg nodes: String) {
        embed.setDescription(nodes.joinToString("\n"))
    }
    fun Body(spaced: Boolean = false, builder: MutableList<String>.() -> Unit) {
        val backing = mutableListOf<String>()
        builder(backing)
        embed.setDescription(if (spaced) backing.joinToString("\n") else backing.joinToString())
    }
    fun Field(name: String, inline: Boolean = false, vararg nodes: String) {
        embed.addField(name, nodes.joinToString(""), inline)
    }
    fun Field(name: String, inline: Boolean = false, spaced: Boolean = false, builder: MutableList<String>.() -> Unit) {
        val backing = mutableListOf<String>()
        builder(backing)
        embed.addField(name, if (spaced) backing.joinToString("\n") else backing.joinToString(), inline)
    }
    fun Image(url: String) {
        embed.setImage(url)
    }
    fun Image(image: Icon) {
        embed.setImage(image)
    }
    fun Image(image: File) {
        embed.setImage(image)
    }
    fun Image(image: InputStream) {
        embed.setImage(image)
    }
    fun Image(image: InputStream, fileType: String) {
        embed.setImage(image, fileType)
    }
    fun Image(image: ByteArray) {
        embed.setImage(image)
    }
    fun Image(image: ByteArray, fileType: String) {
        embed.setImage(image, fileType)
    }
    fun Image(image: BufferedImage) {
        embed.setImage(image)
    }
    fun Image(image: BufferedImage, fileType: String) {
        embed.setImage(image, fileType)
    }
    fun Thumbnail(url: String) {
        embed.setThumbnail(url)
    }
    fun Thumbnail(thumbnail: Icon) {
        embed.setThumbnail(thumbnail)
    }
    fun Thumbnail(thumbnail: File) {
        embed.setThumbnail(thumbnail)
    }
    fun Thumbnail(thumbnail: InputStream) {
        embed.setThumbnail(thumbnail)
    }
    fun Thumbnail(thumbnail: InputStream, fileType: String) {
        embed.setThumbnail(thumbnail, fileType)
    }
    fun Thumbnail(thumbnail: ByteArray) {
        embed.setThumbnail(thumbnail)
    }
    fun Thumbnail(thumbnail: ByteArray, fileType: String) {
        embed.setThumbnail(thumbnail, fileType)
    }
    fun Thumbnail(thumbnail: BufferedImage) {
        embed.setThumbnail(thumbnail)
    }
    fun Thumbnail(thumbnail: BufferedImage, fileType: String) {
        embed.setThumbnail(thumbnail, fileType)
    }
    fun Color(color: Color) {
        embed.setColor(color)
    }
    fun Timestamp(timestamp: Instant) {
        embed.setTimestamp(timestamp)
    }
    fun Footer(text: String) {
        embed.setFooter(text)
    }
    fun Footer(text: String, iconUrl: String) {
        embed.setFooter(text, iconUrl)
    }
    fun Footer(text: String, icon: Icon) {
        embed.setFooter(text, icon)
    }
    fun Footer(text: String, icon: File) {
        embed.setFooter(text, icon)
    }
    fun Footer(text: String, icon: InputStream) {
        embed.setFooter(text, icon)
    }
    fun Footer(text: String, icon: InputStream, fileType: String) {
        embed.setFooter(text, icon, fileType)
    }
    fun Footer(text: String, icon: ByteArray) {
        embed.setFooter(text, icon)
    }
    fun Footer(text: String, icon: ByteArray, fileType: String) {
        embed.setFooter(text, icon, fileType)
    }
    fun Footer(text: String, icon: BufferedImage) {
        embed.setFooter(text, icon)
    }
    fun Footer(text: String, icon: BufferedImage, fileType: String) {
        embed.setFooter(text, icon, fileType)
    }
    fun Author(name: String) {
        embed.setAuthor(name)
    }
    fun Author(author: MessageAuthor) {
        embed.setAuthor(author)
    }
    fun Author(author: User) {
        embed.setAuthor(author)
    }
    fun Author(name: String, url: String, iconUrl: String) {
        embed.setAuthor(name, url, iconUrl)
    }
    fun Author(name: String, url: String, icon: Icon) {
        embed.setAuthor(name, url, icon)
    }
    fun Author(name: String, url: String, icon: File) {
        embed.setAuthor(name, url, icon)
    }
    fun Author(name: String, url: String, icon: InputStream) {
        embed.setAuthor(name, url, icon)
    }
    fun Author(name: String, url: String, icon: InputStream, fileType: String) {
        embed.setAuthor(name, url, icon, fileType)
    }
    fun Author(name: String, url: String, icon: ByteArray) {
        embed.setAuthor(name, url, icon)
    }
    fun Author(name: String, url: String, icon: ByteArray, fileType: String) {
        embed.setAuthor(name, url, icon, fileType)
    }
    fun Author(name: String, url: String, icon: BufferedImage) {
        embed.setAuthor(name, url, icon)
    }
    fun Author(name: String, url: String, icon: BufferedImage, fileType: String) {
        embed.setAuthor(name, url, icon, fileType)
    }
}