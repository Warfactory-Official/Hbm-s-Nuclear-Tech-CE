#version 120
#extension GL_EXT_gpu_shader4 : enable

//Verticals are flipped because for some reason textures are upside down otherwise
const vec2 BOTTOM_LEFT = vec2(-0.5, 0.5);
const vec2 BOTTOM_RIGHT = vec2(0.5, 0.5);
const vec2 TOP_LEFT = vec2(-0.5, -0.5);
const vec2 TOP_RIGHT = vec2(0.5, -0.5);
const int PARTICLE_TEX_SIZE = 1024;
const int MAX_PARTICLE_TYPES = 1;

uniform mat4 modelview;
uniform mat4 projection;
uniform mat4 invPlayerRot;
//Contains position and color
uniform sampler2D particleData0;
//Contains velocity and scale
uniform sampler2D particleData1;
//Contains age, max age, and particle id
uniform sampler2D particleData2;
uniform vec4 particleTypeTexCoords[MAX_PARTICLE_TYPES];

varying vec2 pass_tex;
varying vec2 pass_lightmap;
varying vec4 pass_color;

vec4 colorFromFloat(float f){
	int argb = int(f*2147483647);
	float a = (argb >> 24) & 0xFF;
	float r = (argb >> 16) & 0xFF;
	float g = (argb >> 8) & 0xFF;
	float b = (argb) & 0xFF;
	return vec4(r, g, b, a);
}

void main(){
	vec3 pos = gl_Position.xyz;
	vec2 particle_coord = vec2((gl_InstanceID%PARTICLE_TEX_SIZE)/PARTICLE_TEX_SIZE, 
								(gl_InstanceID/PARTICLE_TEX_SIZE)/PARTICLE_TEX_SIZE);
	vec4 dat0 = texture2DLod(particleData0, particle_coord, 0);
	vec4 dat1 = texture2DLod(particleData1, particle_coord, 0);
	vec4 dat2 = texture2DLod(particleData2, particle_coord, 0);
	
	int particleId = int(dat2.z*32767);
	vec3 offsetPos = dat0.xyz;
	vec4 color = colorFromFloat(dat0.w);
	float scale = dat1.w;
	vec4 texData = particleTypeTexCoords[particleId];
	vec2 lightmap = vec2(0.94117647058, 0.94117647058);

	vec2 tex = texData.xy*float((pos.xy == BOTTOM_LEFT)) +
				vec2(texData.x+texData.z, texData.y)*float((pos.xy == BOTTOM_RIGHT)) +
				vec2(texData.x, texData.y+texData.w)*float((pos.xy == TOP_LEFT)) +
				(texData.xy+texData.zw)*float((pos.xy == TOP_RIGHT));
	
	pass_tex = tex;
	pass_lightmap = lightmap * (255.0 / 256.0) + (8.0 / 256.0);
	pass_color = color;
	
	vec4 position = invPlayerRot * vec4(pos*scale, 1);
	position = modelview * vec4(position.xyz+offsetPos, position.w);
	gl_Position = projection*position;
}
