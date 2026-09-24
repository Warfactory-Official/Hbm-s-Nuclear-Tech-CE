#version 120

//Verticals are flipped because for some reason textures are upside down otherwise
const vec2 BOTTOM_LEFT = vec2(-0.5, 0.5);
const vec2 BOTTOM_RIGHT = vec2(0.5, 0.5);
const vec2 TOP_LEFT = vec2(-0.5, -0.5);
const vec2 TOP_RIGHT = vec2(0.5, -0.5);

attribute vec3 pos;
attribute vec3 offsetPos;
attribute float scale;
attribute vec4 texData;
attribute vec4 color;
attribute vec2 lightmap;

uniform mat4 modelview;
uniform mat4 projection;
uniform mat4 invPlayerRot;

varying vec2 pass_tex;
varying vec2 pass_lightmap;
varying vec4 pass_color;

void main(){
	float right = float(pos.x > 0.0);
	float top = float(pos.y < 0.0);
	vec2 tex = vec2(texData.x + texData.z * right, texData.y + texData.w * top);
			
	pass_tex = tex;
	pass_lightmap = lightmap * (255.0 / 256.0) + (8.0 / 256.0);
	pass_color = color;
	
	vec4 position = invPlayerRot * vec4(pos*scale, 1);
	position = modelview * vec4(position.xyz+offsetPos, position.w);
	gl_Position = projection*position;
}
