package com.creativemd.cmdcam;

import com.creativemd.cmdcam.key.KeyHandler;
import com.creativemd.cmdcam.movement.OutsidePath;
import com.creativemd.cmdcam.utils.CamPoint;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class CamEventHandler {

	public static Minecraft mc = Minecraft.getMinecraft();
	public static float defaultfov = 70.0F;
	public static final float amountZoom = 0.1F;
	public static final float amountroll = 0.5F;
	
	public static boolean selectEntityMode = false;
	
	public static long lastRenderTime;
	
	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if(!selectEntityMode)
			return ;
		
		if(event instanceof EntityInteract)
		{
			CMDCam.target = ((EntityInteract) event).getTarget();
			event.getEntityPlayer().addChatMessage(new TextComponentString("Target is set to " + ((EntityInteract) event).getTarget().getCachedUniqueIdString() + "."));
			selectEntityMode = false;
		}
		
		if(event instanceof RightClickBlock)
		{
			CMDCam.target = event.getPos();
			event.getEntityPlayer().addChatMessage(new TextComponentString("Target is set to " +  event.getPos() + "."));
			selectEntityMode = false;
		}
	}
	
	@SubscribeEvent
	public void onRenderTick(RenderTickEvent event)
	{
		if(mc.thePlayer != null && mc.theWorld != null)
		{
			if(mc.inGameHasFocus) //event.phase == Phase.START )
			{
				if(CMDCam.currentPath == null)
				{
					if(mc.gameSettings.isKeyDown(KeyHandler.zoomIn))
					{
						if(mc.thePlayer.isSneaking())
							mc.gameSettings.fovSetting -= amountZoom*10;
						else
							mc.gameSettings.fovSetting -= amountZoom;
					}
					
					if(mc.gameSettings.isKeyDown(KeyHandler.zoomOut))
					{
						if(mc.thePlayer.isSneaking())
							mc.gameSettings.fovSetting += amountZoom*10;
						else
							mc.gameSettings.fovSetting += amountZoom;
					}
					
					if(mc.gameSettings.isKeyDown(KeyHandler.zoomCenter))
					{
						mc.gameSettings.fovSetting = defaultfov;
					}
					CMDCam.fov = mc.gameSettings.fovSetting;
					
					if(mc.gameSettings.isKeyDown(KeyHandler.rollLeft))
						CMDCam.roll -= amountroll;
					
					if(mc.gameSettings.isKeyDown(KeyHandler.rollRight))
						CMDCam.roll += amountroll;
					
					if(mc.gameSettings.isKeyDown(KeyHandler.rollCenter))
						CMDCam.roll = 0;
					
					if(KeyHandler.pointKey.isPressed())
					{
						CMDCam.points.add(new CamPoint());
						mc.thePlayer.addChatMessage(new TextComponentString("Registered " + CMDCam.points.size() + ". Point!"));
					}
					
					
				}else{
					CMDCam.currentPath.tick(event.renderTickTime);
				}
				
				//ReflectionHelper.setPrivateValue(EntityRenderer.class, mc.entityRenderer, CMDCam.roll, "camRoll", "field_78495_O"); 
				if(KeyHandler.startStop.isPressed())
				{
					if(CMDCam.currentPath != null)
					{
						CMDCam.currentPath.onPathFinished();
						CMDCam.currentPath = null;
					}
					else
						CMDCam.createPath();
				}
			}//else{
				//mc.gameSettings.fovSetting = defaultfov;
			//}
		}
		lastRenderTime = System.nanoTime();
	}
	
	public static boolean shouldPlayerTakeInput()
	{
		return true;
	}
	
	public Entity renderEntity;
	
	@SubscribeEvent
	public void renderPlayerPre(RenderPlayerEvent.Pre event)
	{
		if(CMDCam.currentPath instanceof OutsidePath)
		{
			renderEntity = mc.getRenderManager().renderViewEntity;
			
			mc.getRenderManager().renderViewEntity = mc.thePlayer;
		}
	}
	
	@SubscribeEvent
	public void renderPlayerPost(RenderPlayerEvent.Post event)
	{
		if(CMDCam.currentPath instanceof OutsidePath)
		{
			mc.getRenderManager().renderViewEntity = renderEntity;
		}
	}
	
	@SubscribeEvent
	public void cameraRoll(CameraSetup event)
	{
		event.setRoll(CMDCam.roll);
	}
	
}
