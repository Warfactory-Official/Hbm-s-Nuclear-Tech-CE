#version 120

varying vec2 texCoord;
varying vec2 lightCoord;
varying vec4 color;

uniform sampler2D mainTex;
uniform sampler2D lightmap;

void main(){
	vec4 texColor = texture2D(mainTex, texCoord);
	vec4 lmap = texture2D(lightmap, lightCoord);
	
	float edge = 1-color.a;
	edge = pow(max(edge, 0.01), 1.5);
	float a = clamp(smoothstep(edge, edge+0.1, texColor.b), 0, 1);
	gl_FragColor = vec4((texColor.bbb*0.5+0.5)*color.rgb, a * clamp(texColor.b*3, 0, 1)) * lmap;
}
