package com.example.ai_droid;

import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.RenderableSource;

import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class MainActivity extends AppCompatActivity {

    private ArFragment arFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);

        arFragment.setOnTapArPlaneListener((HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
            if (plane.getType() != Plane.Type.HORIZONTAL_UPWARD_FACING) return;

            RenderableSource source = RenderableSource.builder()
                    .setSource(this, Uri.parse("model.glb"), RenderableSource.SourceType.GLB)
                    .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                    .build();

            ModelRenderable.builder()
                    .setSource(this, source)
                    .setRegistryId("model.glb")
                    .build()
                    .thenAccept(modelRenderable -> {
                        AnchorNode anchorNode = new AnchorNode(hitResult.createAnchor());
                        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
                        model.setRenderable(modelRenderable);
                        model.setParent(anchorNode);
                        arFragment.getArSceneView().getScene().addChild(anchorNode);
                        model.select();
                    })
                    .exceptionally(throwable -> {
                        throwable.printStackTrace();
                        return null;
                    });
        });
    }
}
